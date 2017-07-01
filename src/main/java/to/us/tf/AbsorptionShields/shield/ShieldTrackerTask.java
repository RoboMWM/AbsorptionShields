package to.us.tf.AbsorptionShields.shield;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import to.us.tf.AbsorptionShields.ConfigManager;

/**
 * Created on 6/30/2017.
 * In lieu of a proper ArmorEquipEvent (there "is" one out there though it listens to a
 * bazillion other events to do what it does thanks to MC's inventory handling)
 *
 * @author RoboMWM
 */
public class ShieldTrackerTask extends BukkitRunnable
{
    JavaPlugin instance;
    ShieldManager shieldManager;
    ConfigManager configManager;

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
            Shield shield = (Shield)player.getMetadata("AS_SHIELD").get(0).value();
            String wearingShieldName = shieldManager.getWornShield(player);

            if (shield == null && wearingShieldName == null)
                continue;

            //Add a shield
            if (shield == null && wearingShieldName != null)
            {
                player.setMetadata("AS_SHIELD", new FixedMetadataValue(instance, configManager.createShield(wearingShieldName, true)));
                continue;
            }

            if (shield != null)
            {
                //Delete a shield
                if (wearingShieldName == null || wearingShieldName.equalsIgnoreCase(shield.getName()))
                {
                    player.removeMetadata("AS_SHIELD", instance);
                    shieldManager.shatterShield(player);
                    continue;
                }
            }
        }
    }



}
