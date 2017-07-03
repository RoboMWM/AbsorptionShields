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
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import to.us.tf.absorptionshields.ConfigManager;

import java.util.HashSet;
import java.util.Set;

/**
 * Created on 3/18/2017.
 *
 * @author RoboMWM
 */
public class ShieldManager implements Listener
{
    ShieldUtils shieldUtils;
    ConfigManager configManager;

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

    public ShieldManager(JavaPlugin plugin, ShieldUtils shieldUtils, ConfigManager configManager)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.shieldUtils = shieldUtils;
        this.configManager = configManager;

        //Schedule tasks
        new ShieldRegeneratationTask(this, shieldUtils, 5L).runTaskTimer(plugin, 300L, 5L);
        new ShieldTrackerTask(plugin, this, configManager).runTaskTimer(plugin, 300L, 20L);
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onQuit(PlayerQuitEvent event)
    {
        playersWithDamagedShields.remove(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void onPlayerDamaged(EntityDamageEvent event)
    {
        if (event.getEntityType() != EntityType.PLAYER)
            return;

        Player player = (Player)event.getEntity();

        if (!hasShield(player))
            return;

        getShield(player).resetRegenCounter();

        float shieldHealth = shieldUtils.getShieldHealth(player);
        if (shieldHealth <= 0f)
            return;

        //cache player for shield regeneration
        playersWithDamagedShields.add(player);

        //DamageModifier API is deprecated and will likely be removed soon; this'll have to do.
        //TODO: does getDamage factor in damage before armor/absorption/etc.???????? (I'm assuming it does...)

        shieldHealth -= event.getDamage();

        //Shield broken
        if (shieldHealth < 0)
        {
            event.setDamage(-shieldHealth);
            shatterShield(player);
            player.playSound(player.getLocation(), "fortress.shieldoffline", SoundCategory.PLAYERS, 3000000f, 1.0f);
            return;
        }

        event.setDamage(0);
        shieldUtils.setShieldHealth(player, shieldHealth);
        //TODO: make configurable; distance check required?
        //TODO: pitch variation
        for (Player p : player.getWorld().getPlayers())
        {
            if (p == player)
                continue;
            player.getWorld().playSound(player.getLocation(), "fortress.shieldhit", SoundCategory.PLAYERS, 1.0f, 1.0f);
        }
        player.playSound(player.getLocation(), "fortress.shieldhitself", SoundCategory.PLAYERS, 3000000f, 1.0f);

        //TODO: use player#setglowing (to avoid particles)
        player.addPotionEffect(PotionEffectType.GLOWING.createEffect(8, 0));
    }

    //Shields prevent armor from taking damage (since they ignore armor resistances)
    @EventHandler(ignoreCancelled = true)
    private void onArmorDamage(PlayerItemDamageEvent event)
    {
        if (shieldUtils.getShieldHealth(event.getPlayer()) <= 0)
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
        if (shieldUtils.getShieldHealth(player) <= 0)
            return;
        shieldUtils.setShieldHealth(player, 0);

        //TODO: allow customization
        player.getWorld().playSound(player.getLocation(), "fortress.shieldbroken", SoundCategory.PLAYERS, 1.0f, 1.0f);
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

