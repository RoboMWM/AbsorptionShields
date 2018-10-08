package com.robomwm.absorptionshields;

import com.robomwm.absorptionshields.shield.Shield;
import com.robomwm.absorptionshields.shield.ShieldManager;
import com.robomwm.customitemrecipes.CustomItemRecipes;
import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import com.robomwm.absorptionshields.command.AddShieldLoreCommand;
import com.robomwm.absorptionshields.shield.ShieldUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created on 3/18/2017.
 *
 * @author RoboMWM
 */
public class AbsorptionShields extends JavaPlugin
{
    private CustomItemRecipes customItemRecipes;
    private ConfigManager configManager;
    private ShieldUtils shieldUtils;

    public ShieldUtils getShieldUtils()
    {
        return shieldUtils;
    }

    public void onEnable()
    {
        try
        {
            customItemRecipes = (CustomItemRecipes)getServer().getPluginManager().getPlugin("CustomItemRecipes");
        }
        catch (Throwable rock)
        {
            getLogger().warning("CustomItemRecipes is not installed.");
            getLogger().warning("Only shields in config using Material enum names will function; the rest will not be detected.");
            getLogger().warning("Get CustomItemRecipes to define your own custom items to use as shields!");
            getLogger().warning("http://r.robomwm.com/cir");
        }


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
        getCommand("addshieldstats").setExecutor(new AddShieldLoreCommand(this, configManager));

        try
        {
            Metrics metrics = new Metrics(this);
            metrics.addCustomChart(new Metrics.SimplePie("bukkit_implementation", new Callable<String>()
            {
                @Override
                public String call() throws Exception
                {
                    return getServer().getVersion().split("-")[1];
                }
            }));

            for (final String key : getConfig().getKeys(false))
            {
                if (!getConfig().isBoolean(key) && !getConfig().isInt(key) && !getConfig().isString(key))
                    continue;
                metrics.addCustomChart(new Metrics.SimplePie(key.toLowerCase(), new Callable<String>()
                {
                    @Override
                    public String call() throws Exception
                    {
                        return getConfig().getString(key);
                    }
                }));
            }
        }
        catch (Throwable ignored) {}
    }

    public CustomItemRecipes getCustomItemRecipes()
    {
        return customItemRecipes;
    }

    public void onDisable()
    {
        for (Player player : getServer().getOnlinePlayers())
            player.removeMetadata("AS_SHIELD", this);
    }

    public ConfigManager getConfigManager()
    {
        return configManager;
    }

    public List<String> getStats(Shield shield)
    {
        List<String> lore = new ArrayList<>();
        DecimalFormat df = new DecimalFormat("#.##");

        //TODO: configurable...
        lore.add(ChatColor.YELLOW + "AbsorptionShield Stats:");
        lore.add(ChatColor.GOLD + "- Capacity: " + ChatColor.YELLOW + shield.getMaxShieldStrength() / 2 + "hp");
        lore.add(ChatColor.GOLD + "- Recharge Rate: " + ChatColor.YELLOW + shield.getRegenRate() + "hp/s");
        lore.add(ChatColor.GOLD + "- Recharge Delay: " + ChatColor.YELLOW + df.format(shield.getRegenTime() / 20L) + "s");

        return lore;
    }

    public ItemStack appendShieldStats(String name, ItemStack itemStack)
    {
        ItemMeta itemMeta = itemStack.getItemMeta(); //I guess all items have metadata, since there's no way to construct new ones...

        Shield shield = configManager.createShield(name, false);
        if (shield == null)
            return null;

        int i = 0;
        List<String> lore;

        if (itemMeta.hasLore())
        {
            lore = itemMeta.getLore();
            for (i = 0; i < lore.size(); i++)
                if (lore.get(i).equalsIgnoreCase(ChatColor.YELLOW + "AbsorptionShield Stats:")) //Replace existing stats, if present
                {
                    //Aren't stringlists lovely
                    lore.remove(i);
                    lore.remove(i);
                    lore.remove(i);
                    lore.remove(i);
                    break;
                }
        }
        else
            lore = new ArrayList<>();

        //Append stats
        lore.addAll(i, getStats(shield));

        //Set Display name
        itemMeta.setDisplayName(shield.getFormattedName());

        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    /**
     * So, I was gonna add this to Usefulutil.
     * But there's no good way to implement variable priority while maintaining a unique ID w/o
     * having to create a custom datatype.
     *
     * So yea... maybe I'll implement a better version in GrandioseAPI or something...
     * @param player
     * @param durationInTicks
     */
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
