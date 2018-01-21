package com.robomwm.absorptionshields.command;

import com.robomwm.absorptionshields.AbsorptionShields;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Created on 7/30/2017.
 *
 * @author RoboMWM
 */
public class AddLoreCommand implements CommandExecutor
{
    AbsorptionShields instance;

    public AddLoreCommand(AbsorptionShields plugin)
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
            player.sendMessage("You need an item in your hand to add lore to said shield item.");
            return false;
        }

        if (args.length < 1)
        {
            return false;
        }

        ItemStack itemStack = player.getInventory().getItemInMainHand();

        player.getInventory().setItemInMainHand(instance.addLore(String.join(" ", args), itemStack));

        return true;
    }
}
