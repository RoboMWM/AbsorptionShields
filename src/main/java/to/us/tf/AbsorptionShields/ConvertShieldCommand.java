package to.us.tf.absorptionshields;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Created on 7/1/2017.
 *
 * Converts the held item into a shield
 *
 * @author RoboMWM
 */
public class ConvertShieldCommand implements CommandExecutor
{
    AbsorptionShields instance;
    ConfigManager configManager;

    ConvertShieldCommand(AbsorptionShields plugin, ConfigManager configManager)
    {
        instance = plugin;
        this.configManager = configManager;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!(sender instanceof Player))
        {
            sender.sendMessage("le sigh");
            return false;
        }

        Player player = (Player)sender;

        if (player.getInventory().getItemInMainHand() == null
                || player.getInventory().getItemInMainHand().getType() == Material.AIR)
        {
            player.sendMessage("You need an item in your hand to convert into a shield.");
            return false;
        }

        if (args.length < 1)
        {
            player.sendMessage("/" + label + " <shieldName>");
            player.sendMessage("Available shields: ");
            StringBuilder stringBuilder = new StringBuilder();
            for (String shieldName : configManager.getShieldNames())
            {
                stringBuilder.append(shieldName);
                stringBuilder.append(", ");
            }
            stringBuilder.setLength(stringBuilder.length() - 2);
            player.sendMessage(stringBuilder.toString());
            return false;
        }

        ItemStack itemStack = player.getInventory().getItemInMainHand();

        player.getInventory().setItemInMainHand(instance.convertToShield(String.join(" ", args), itemStack));

        return true;
    }
}
