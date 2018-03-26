package com.robomwm.absorptionshields.command;

import com.robomwm.absorptionshields.AbsorptionShields;
import com.robomwm.absorptionshields.ConfigManager;
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
public class AddShieldLoreCommand implements CommandExecutor
{
    private AbsorptionShields instance;
    private ConfigManager configManager;

    public AddShieldLoreCommand(AbsorptionShields plugin, ConfigManager configManager)
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
            printShields(player);
            return false;
        }

        ItemStack itemStack = instance.appendShieldStats(String.join(" ", args), player.getInventory().getItemInMainHand());
        if (itemStack == null)
        {
            player.sendMessage("Invalid shield specified.");
            printShields(player);
            return true;
        }
        player.getInventory().setItemInMainHand(itemStack);
        player.sendMessage("Appended stats in the lore of this item. Please note that you must register this item with CustomItemRecipes (via /citem <shield_name>) before this item is recognized as an AbsorptionShield.");

        return true;
    }

    private void printShields(Player player)
    {
        player.sendMessage("Available shields: ");
        StringBuilder stringBuilder = new StringBuilder();
        for (String shieldName : configManager.getShieldNames())
        {
            stringBuilder.append(shieldName);
            stringBuilder.append(", ");
        }
        stringBuilder.setLength(stringBuilder.length() - 2);
        player.sendMessage(stringBuilder.toString());
    }
}
