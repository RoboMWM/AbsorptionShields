package to.us.tf.AbsorptionShields;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import to.us.tf.AbsorptionShields.shield.ShieldManager;
import to.us.tf.AbsorptionShields.shield.ShieldUtils;

/**
 * Created on 3/18/2017.
 *
 * @author RoboMWM
 */
public class AbsorptionShields extends JavaPlugin
{
    ConfigManager configManager;

    public void onEnable()
    {
        ShieldUtils shieldUtils = null;
        try
        {
            shieldUtils = new ShieldUtils(this);
        }
        catch (Exception e)
        {
            this.getLogger().severe("So uh yea I think Minecraft must've changed how they set absorption hearts or something.");
            e.printStackTrace();
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        configManager = new ConfigManager(this);
        new ShieldManager(this, shieldUtils, configManager);
        getCommand("makeshield").setExecutor(new ConvertShieldCommand(this, configManager));
    }

    public ItemStack convertToShield(String shieldName, ItemStack itemStack)
    {
        if (!configManager.isValidShieldName(shieldName, false))
            return null;

        ItemMeta itemMeta = itemStack.getItemMeta(); //I guess all items have metadata, since there's no way to construct new ones...
        itemMeta.setDisplayName(configManager.createShield(shieldName, false).getName());
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
