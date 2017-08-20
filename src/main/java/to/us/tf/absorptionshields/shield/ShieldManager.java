package to.us.tf.absorptionshields.shield;

import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import to.us.tf.absorptionshields.AbsorptionShields;
import to.us.tf.absorptionshields.ConfigManager;
import to.us.tf.absorptionshields.event.ShieldDamageEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created on 3/18/2017.
 *
 * @author RoboMWM
 */
public class ShieldManager implements Listener
{
    AbsorptionShields instance;
    ShieldUtils shieldUtils;
    ConfigManager configManager;
    ShieldTrackerTask shieldTrackerTask;

    private Set<Player> playersWithDamagedShields = new HashSet<>(); //Cache of players who need shields to be regenerated

    public Set<Player> getPlayersWithDamagedShields()
    {
        return new HashSet<>(playersWithDamagedShields);
    }

    public void addPlayerWithDamagedShield(Player player)
    {
        playersWithDamagedShields.add(player);
    }

    public void removePlayerWithDamagedShield(Player player)
    {
        playersWithDamagedShields.remove(player);
    }

    public ShieldManager(AbsorptionShields plugin, ShieldUtils shieldUtils, ConfigManager configManager)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.shieldUtils = shieldUtils;
        this.configManager = configManager;
        this.instance = plugin;

        //Schedule tasks
        new ShieldRegeneratationTask(this, shieldUtils, 5L).runTaskTimer(plugin, 300L, 5L);
        shieldTrackerTask = new ShieldTrackerTask(plugin, this, configManager);
        shieldTrackerTask.runTaskTimer(plugin, 300L, 20L);
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    private void onQuit(PlayerQuitEvent event)
    {
        shieldTrackerTask.addRemoveShield(event.getPlayer());
        playersWithDamagedShields.remove(event.getPlayer());
        event.getPlayer().removeMetadata("AS_SHIELD", instance);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onPlayerDamaged(EntityDamageEvent event)
    {
        if (event.getEntityType() != EntityType.PLAYER)
            return;

        Player player = (Player)event.getEntity();

        if (!hasShield(player))
            return;

        getShield(player).resetRegenCounter();
        playersWithDamagedShields.add(player);
        Shield shield = getShield(player);
        shield.resetRegenCounter();

        final double originalDamage = event.getDamage(); //We might need to get this from a lower-priority listener.
        final float originalShieldHealth = shieldUtils.getShieldHealth(player);
        float shieldHealth = originalShieldHealth;
        if (shieldHealth <= 0f)
            return;

        //DamageModifier API is deprecated and will likely be removed soon; this'll have to do.
        //getDamage factors in damage before armor/absorption/etc.

        shieldHealth -= event.getDamage();

        //TODO: make configurable
        //Shield broken
        if (shieldHealth <= 0f)
        {
            //The following was removed since CB or whatever handles this for us
            //shieldHealth = -shieldHealth; //Unless -shieldHealth does this already. Idk. I don't use the - operator all that much.
            //event.setDamage(shieldHealth);
            shatterShield(player);
            configManager.playSound(player, "shieldOfflineAlert", false);
            instance.getServer().getPluginManager().callEvent(new ShieldDamageEvent(player, originalShieldHealth, event));
            return;
        }

        shieldUtils.setShieldHealth(player, shieldHealth);

        switch (event.getCause())
        {
            case FALL:
            case DROWNING:
                break;
            default:
                //TODO: make configurable
                if (shieldHealth > shield.getMaxShieldStrength() / 3f)
                    configManager.playSound(player, "shieldHitSelf", true);
                else
                    configManager.playSound(player, "lowShieldHitSelf", true);
        }

        instance.timedGlow(player, 8L);

        instance.getServer().getPluginManager().callEvent(new ShieldDamageEvent(player, event.getDamage(), event));

        event.setDamage(0);

//        if (event.getFinalDamage() != 0)
//            event.setDamage(-event.getFinalDamage());
    }

    /*The resistance modifier is computed according to the original damage value.
    //If damage is over half the current shield strength, the resistance modifier will cause #getFinalDamage to return a negative value
    //This is because the player now has less absorption hearts to cover the cost of the _original damage._
    //Unfortunately, CB appears to just take the absolute value of #getFinalDamage and damage the entity anyways...
    HOWEVER!
    Even though the resistance value uses the original damage value in its computation,
     this computation can be recalculated with new resistance attributes via calling event#setDamage (I think)!
    */

    //Shields prevent armor from taking damage (since they ignore armor resistances)
    @EventHandler(ignoreCancelled = true)
    private void onArmorDamage(PlayerItemDamageEvent event)
    {
        if (shieldUtils.getShieldHealth(event.getPlayer()) <= 0f)
            return;

        if (isArmor(event.getItem().getType()))
            event.setCancelled(true);
    }

    public boolean hasShield(Player player)
    {
        return player.hasMetadata("AS_SHIELD");
    }

    /**
     * Get the shield currently active on the player
     * @param player
     * @return
     */
    public Shield getShield(Player player)
    {
        if (!hasShield(player))
            return null;
        return (Shield)player.getMetadata("AS_SHIELD").get(0).value();
    }

    /**
     * Sets absorption to 0 if not already set to 0
     * @param player
     */
    public void shatterShield(Player player)
    {
        if (shieldUtils.getShieldHealth(player) <= 0f)
            return;
        shieldUtils.setShieldHealth(player, 0f);
        configManager.playSound(player, "shieldBroken", false);
    }

    /**
     * Get the name of the shield worn on the player (i.e. name of helmet item)
     * TODO: find a better name...
     * @param player
     * @return name of the shield; null otherwise (not wearing a shield)
     */
    public String getWornShield(Player player)
    {
        ItemStack helmet = player.getInventory().getHelmet();
        if (helmet == null)
            return null;
        if (!helmet.hasItemMeta())
            return null;

        ItemMeta helmetMeta = helmet.getItemMeta();

        if (!helmetMeta.hasDisplayName())
            return null;

        String name = helmetMeta.getDisplayName();

        if (!configManager.isValidShieldName(name, true))
            return null;

        return name;
    }

    /**
     * Ew
     * @param material
     * @return if the item is a piece of armor
     */
    public boolean isArmor(Material material)
    {
        switch (material)
        {
            case CHAINMAIL_HELMET:
            case LEATHER_HELMET:
            case GOLD_HELMET:
            case IRON_HELMET:
            case DIAMOND_HELMET:
            case GOLD_CHESTPLATE:
            case IRON_CHESTPLATE:
            case DIAMOND_CHESTPLATE:
            case LEATHER_CHESTPLATE:
            case CHAINMAIL_CHESTPLATE:
            case GOLD_LEGGINGS:
            case IRON_LEGGINGS:
            case DIAMOND_LEGGINGS:
            case LEATHER_LEGGINGS:
            case CHAINMAIL_LEGGINGS:
            case DIAMOND_BOOTS:
            case GOLD_BOOTS:
            case CHAINMAIL_BOOTS:
            case IRON_BOOTS:
            case LEATHER_BOOTS:
                return true;
        }
        return false;
    }




}

