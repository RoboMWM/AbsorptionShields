package to.us.tf.absorptionshields;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import to.us.tf.absorptionshields.shield.ShieldManager;
import to.us.tf.absorptionshields.shield.ShieldUtils;

import java.util.List;

/**
 * Created on 3/18/2017.
 *
 * @author RoboMWM
 */
public class AbsorptionShields extends JavaPlugin
{
    ConfigManager configManager;
    ShieldUtils shieldUtils;

    public ShieldUtils getShieldUtils()
    {
        return shieldUtils;
    }

    public void onEnable()
    {

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
        getCommand("createshield").setExecutor(new ConvertShieldCommand(this, configManager));
    }

    public void onDisable()
    {
        for (Player player : getServer().getOnlinePlayers())
            player.removeMetadata("AS_SHIELD", this);
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

    public void timedGlow(Player player, long durationInTicks)
    {
        final JavaPlugin plugin = this;
        final String key = "GLOWING";
        final boolean overrideOtherPlugins = false;
        player.removeMetadata(key, plugin);

        //Check if another plugin has "overriden"
        if (!overrideOtherPlugins && player.hasMetadata(key))
        {
            boolean isSetElsewhere = false;
            for (MetadataValue value : player.getMetadata(key))
            {
                if (value.getOwningPlugin() != plugin)
                {
                    isSetElsewhere = true;
                    break;
                }
            }

            //If so, abort
            if (isSetElsewhere)
                return;
        }

        final long timeId = System.currentTimeMillis();

        player.setMetadata(key, new FixedMetadataValue(plugin, timeId));
        player.setGlowing(true);

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                List<MetadataValue> metadata = player.getMetadata(key);
                if (metadata == null)
                    return;

                //Check if another plugin has "overriden"
                if (!overrideOtherPlugins && metadata.size() > 1)
                {
                    boolean isSetElsewhere = false;
                    for (MetadataValue value : player.getMetadata(key))
                    {
                        if (value.getOwningPlugin() != plugin)
                        {
                            isSetElsewhere = true;
                            break;
                        }
                    }
                    //If so, remove our metadata if we have set any and abort
                    if (isSetElsewhere)
                    {
                        player.removeMetadata(key, plugin);
                        return;
                    }
                }

                if (player.getMetadata("GLOWING").get(0).asLong() != timeId)
                    return;

                player.setGlowing(false);
                player.removeMetadata("GLOWING", plugin);
            }
        }.runTaskLater(plugin, durationInTicks);
    }
}
