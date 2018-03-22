package com.robomwm.absorptionshields.shield;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import com.robomwm.absorptionshields.ConfigManager;

/**
 * Created on 6/30/2017.
 * In lieu of a proper ArmorEquipEvent (there "is" one out there though it listens to a
 * bazillion other events to do what it does thanks to MC's inventory handling)
 *
 * @author RoboMWM
 */
public class ShieldTrackerTask extends BukkitRunnable
{
    private JavaPlugin instance;
    private ShieldManager shieldManager;
    private ConfigManager configManager;

    ShieldTrackerTask(JavaPlugin plugin, ShieldManager shieldManager, ConfigManager configManager)
    {
        this.instance = plugin;
        this.shieldManager = shieldManager;
        this.configManager = configManager;
    }

    @Override
    public void run()
    {
        for (Player player : instance.getServer().getOnlinePlayers())
        {
            registerOrUnregisterShield(player);
        }
    }

    public void registerOrUnregisterShield(Player player)
    {
        Shield shield = shieldManager.getShield(player);
        String wearingShieldName = shieldManager.getWornShieldName(player);

        //Not wearing a shield, nor is a shield registered
        if (shield == null && wearingShieldName == null)
            return;

        //Shield is registered, but not wearing it anymore/wearing a different shield
        if (shield != null && (wearingShieldName == null || !wearingShieldName.equals(shield.getName())))
        {
            player.removeMetadata("AS_SHIELD", instance);
            shieldManager.shatterShield(player);

            //used to update HealthBar and other plugins that check absorption, etc.
            EntityRegainHealthEvent event = new EntityRegainHealthEvent(player, 0, EntityRegainHealthEvent.RegainReason.CUSTOM);
            Bukkit.getPluginManager().callEvent(event);
            return;
        }

        //Wearing a shield, but not registered yet
        if (shield == null)
        {
            player.setMetadata("AS_SHIELD", new FixedMetadataValue(instance, configManager.createShield(wearingShieldName, true)));
            shieldManager.addPlayerWithDamagedShield(player);
            return;
        }


    }



}
