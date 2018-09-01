package com.robomwm.absorptionshields.shield;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import com.robomwm.absorptionshields.AbsorptionShields;
import com.robomwm.absorptionshields.ConfigManager;
import com.robomwm.absorptionshields.event.ShieldDamageEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * Created on 3/18/2017.
 *
 * @author RoboMWM
 */
public class ShieldManager implements Listener
{
    private AbsorptionShields plugin;
    private ShieldUtils shieldUtils;
    private ConfigManager configManager;
    private ShieldTrackerTask shieldTrackerTask;

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
        this.plugin = plugin;

        //Schedule tasks
        new ShieldRegeneratationTask(this.plugin, this, shieldUtils, 5L).runTaskTimer(plugin, 5L, 5L);
        shieldTrackerTask = new ShieldTrackerTask(plugin, this, configManager);
        shieldTrackerTask.runTaskTimer(plugin, 300L, 20L);
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    private void onQuit(PlayerQuitEvent event)
    {
        shieldTrackerTask.registerOrUnregisterShield(event.getPlayer());
        playersWithDamagedShields.remove(event.getPlayer());
        event.getPlayer().removeMetadata("AS_SHIELD", plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR) //TODO: change priority back to high once crackshot alternative exists
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

        final double originalDamage = event.getDamage(); //We might need to get this from a lower-priority listener, in case a plugin uses #setDamage.
        final float originalShieldHealth = shieldUtils.getShieldHealth(player);
        if (originalShieldHealth <= 0f)
            return;

        float shieldHealth = originalShieldHealth;

        //DamageModifier API is deprecated and will likely be removed soon; this'll have to do.
        //getDamage factors in damage before armor/absorption/etc.

        shieldHealth -= event.getDamage();

        //Shield broken
        if (shieldHealth <= 0f)
        {
            //Remove the absorption hearts _first_
            shatterShield(player);
            //Then set the raw damage (event#setDamage currently does a bunch of other junk)
            event.setDamage(EntityDamageEvent.DamageModifier.BASE, -shieldHealth);
            //Remove absorption resistance modifier from event#getFinalDamage calculation (please _properly_ recalculate resistances if you get rid of the DamageModifier API, md_5.)
            event.setDamage(EntityDamageEvent.DamageModifier.ABSORPTION, 0);

            //If DamageModifiers do indeed disappear:
            //event.setDamage(originalDamage + originalShieldHealth); //ensures we apply enough damage to surpass the absorption modifier, but still retain other resistances

            configManager.playSound(player, "shieldOfflineAlert", false);
            plugin.getServer().getPluginManager().callEvent(new ShieldDamageEvent(player, originalShieldHealth, event));
            return;
        }

        //event#setDamage causes the resistance modifier to be recalculated with the _original_ damage value
        //So we set it _before_ modifying resistance attributes, such as absorption hearts.
        //This way, we avoid event#getFinalDamage from becoming a negative value (and thus dealing extra damage to absorption hearts).
        //https://hub.spigotmc.org/jira/browse/SPIGOT-3484
        //Update: We'll just set raw damage anyways just in case
        event.setDamage(EntityDamageEvent.DamageModifier.BASE, 0);
        //resistance modifiers aren't updated if we just modify the base damage apparently...
        //Technically, we should 0 out all the other resistance modifiers, but we already have the damage for those blocked in PlayerItemDamageEvent.
        event.setDamage(EntityDamageEvent.DamageModifier.ABSORPTION, 0);

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

        plugin.timedGlow(player, 8L);

        plugin.getServer().getPluginManager().callEvent(new ShieldDamageEvent(player, originalDamage, event));
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
     * Get the name of the shield worn on the player (i.e. name of chestplate item)
     * @param player
     * @return name of the shield; null otherwise (not wearing a shield)
     */
    public String getWornShieldName(Player player)
    {
        ItemStack armorPiece = configManager.getArmorItem(player);
        if (armorPiece == null)
            return null;

        String name = plugin.getCustomItemRecipes().extractCustomID(armorPiece.getItemMeta());
        if (name == null)
            return armorPiece.getType().name();
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
            case GOLDEN_HELMET:
            case IRON_HELMET:
            case DIAMOND_HELMET:
            case GOLDEN_CHESTPLATE:
            case IRON_CHESTPLATE:
            case DIAMOND_CHESTPLATE:
            case LEATHER_CHESTPLATE:
            case CHAINMAIL_CHESTPLATE:
            case GOLDEN_LEGGINGS:
            case IRON_LEGGINGS:
            case DIAMOND_LEGGINGS:
            case LEATHER_LEGGINGS:
            case CHAINMAIL_LEGGINGS:
            case DIAMOND_BOOTS:
            case GOLDEN_BOOTS:
            case CHAINMAIL_BOOTS:
            case IRON_BOOTS:
            case LEATHER_BOOTS:
                return true;
        }
        return false;
    }
}

