package to.us.tf.absorptionshields.command;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import to.us.tf.absorptionshields.AbsorptionShields;

/**
 * Created on 7/30/2017.
 *
 * @author RoboMWM
 */
public class DeleteLoreCommand implements CommandExecutor
{
    AbsorptionShields instance;

    public DeleteLoreCommand(AbsorptionShields plugin)
    {
        instance = plugin;
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
            player.sendMessage("You need an item in your hand to delete lore from said shield item.");
            return false;
        }

        ItemStack itemStack = player.getInventory().getItemInMainHand();

        player.getInventory().setItemInMainHand(instance.deleteLore(itemStack));

        return true;
    }
}
