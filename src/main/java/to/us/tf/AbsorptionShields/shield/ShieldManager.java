package to.us.tf.AbsorptionShields.shield;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import to.us.tf.AbsorptionShields.ConfigManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
        return playersWithDamagedShields;
    }

    public ShieldManager(JavaPlugin plugin, ShieldUtils shieldUtils)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        //Schedule tasks
        new ShieldRegeneratationTask(this, shieldUtils, 5L).runTaskTimer(plugin, 300L, 5L);
        new ShieldTrackerTask(plugin, configManager).runTaskTimer(plugin, 300L, 20L);
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

        //DamageModifier API is deprecated and will likely be removed soon; this'll have to do.
        //TODO: does getDamage factor in damage before armor/absorption/etc.???????? (I'm assuming it does...)

        shieldHealth -= event.getDamage();

        //Shield broken
        if (shieldHealth < 0)
        {
            event.setDamage(-shieldHealth);
            shatterShield(player);
            return;
        }

        event.setDamage(0);
        shieldUtils.setShieldHealth(player, shieldHealth);
        //TODO: shield damage sound effect
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
        //TODO: sound effect
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

        if (!configManager.isValidShieldName(name))
            return null;

        return name;
    }
}

