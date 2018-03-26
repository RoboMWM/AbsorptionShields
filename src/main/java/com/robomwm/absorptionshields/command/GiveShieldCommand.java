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
public class GiveShieldCommand implements CommandExecutor
{
    private AbsorptionShields instance;
    private ConfigManager configManager;

    public GiveShieldCommand(AbsorptionShields plugin, ConfigManager configManager)
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

        player.getInventory().addItem(instance.getShieldItem(String.join(" ", args)));

        return true;
    }
}
