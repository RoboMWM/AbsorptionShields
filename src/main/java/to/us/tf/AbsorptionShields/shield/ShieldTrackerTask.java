package to.us.tf.AbsorptionShields.shield;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created on 6/30/2017.
 * In lieu of a proper ArmorEquipEvent (there "is" one out there though it listens to a
 * bazillion other events to do what it does thanks to MC's inventory handling)
 *
 * @author RoboMWM
 */
public class ShieldTrackerTask implements Runnable
{
    JavaPlugin plugin;

    @Override
    public void run()
    {
        for (Player player : plugin.getServer().getOnlinePlayers())
        {
            Shield shield = (Shield)player.getMetadata("AS_SHIELD").get(0).value();

            if (shield == null && helmet == null)
                continue;

            if (helmet.hasItemMeta())

        }
    }

    private String helmetShieldName(Player player)
    {
        ItemStack helmet = player.getInventory().getHelmet();
        if (helmet == null)
            return null;

    }
}
