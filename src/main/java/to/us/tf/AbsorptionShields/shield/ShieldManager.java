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
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

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

    Set<Player> playersWithDamagedShields = new HashSet<>(); //Cache of players who need shields to be regenerated


    public ShieldManager(JavaPlugin plugin, ShieldUtils shieldUtils)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        //Shield regeneration task
        //Initially I was going to stick this in the Shield object but a) would have to cancel task when object is "deleted" and b) would have to pass a plugin instance to each.
        final long rateToCheck = 5L;
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                for (Player player : playersWithDamagedShields)
                {
                    if (!hasShield(player))
                    {
                        playersWithDamagedShields.remove(player);
                        continue;
                    }

                    Shield shield = getShield(player);

                    //Not ready to regenerate yet
                    if (!shield.incrementCounter(5L))
                        continue;

                    float shieldHealth = shieldUtils.getShieldHealth(player);

                    //All regenerated
                    if (shieldHealth >= shield.getMaxShieldStrength())
                    {
                        shieldUtils.setShieldHealth(player, shield.getMaxShieldStrength());
                        playersWithDamagedShields.remove(player);
                        //TODO: shield regeneration complete sound
                        continue;
                    }

                    //Regen
                    if (shieldHealth < shield.getMaxShieldStrength())
                    {
                        float amountToRegen = shield.getRegenRate() / (20L / rateToCheck);
                        float missingShield = shield.getMaxShieldStrength() - shieldHealth;
                        //TODO: regeneration sound effect with pitch on missingShield.

                        //Top off if near full
                        if (amountToRegen > missingShield)
                        {
                            shieldUtils.setShieldHealth(player, shield.getMaxShieldStrength());
                        }
                        else
                            shieldUtils.setShieldHealth(player, shieldHealth + amountToRegen);
                    }
                }
            }
        }.runTaskTimer(plugin, 300L, rateToCheck);

        //TODO: start shield tracker
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

        resetRegenTime(player);

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
            shieldUtils.setShieldHealth(player, 0f);
            //TODO: shield broken sound effect
            return;
        }

        event.setDamage(0);
        shieldUtils.setShieldHealth(player, shieldHealth);
        //TODO: shield damage sound effect
    }

    private boolean hasShield(Player player)
    {
        return player.hasMetadata("AS_SHIELD");
    }

    private void resetRegenTime(Player player)
    {

    }

    private Shield getShield(Player player)
    {
        if (!hasShield(player))
            return null;
        return (Shield)player.getMetadata("AS_SHIELD").get(0).value();
    }
}

