package com.robomwm.absorptionshields;

import com.robomwm.absorptionshields.shield.Shield;
import com.robomwm.absorptionshields.shield.ShieldManager;
import com.robomwm.customitemrecipes.CustomItemRecipes;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import com.robomwm.absorptionshields.command.GiveShieldCommand;
import com.robomwm.absorptionshields.shield.ShieldUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

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
        customItemRecipes = (CustomItemRecipes)getServer().getPluginManager().getPlugin("CustomItemRecipes");

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
        getCommand("giveshield").setExecutor(new GiveShieldCommand(this, configManager));
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

    public ItemStack getShieldItem(String shieldName)
    {
        if (!configManager.isValidShieldName(shieldName, false))
            return null;

        ItemStack shieldItem = customItemRecipes.getItem(shieldName);
        if (shieldItem == null)
            return null;
        Shield shield = configManager.createShield(shieldName, false);
        ItemMeta itemMeta = shieldItem.getItemMeta();
        itemMeta.setDisplayName(shield.getName());
        shieldItem.setItemMeta(itemMeta);
        appendShieldStats(shieldItem);
        return shieldItem;
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

    public ItemStack appendShieldStats(ItemStack itemStack)
    {
        ItemMeta itemMeta = itemStack.getItemMeta(); //I guess all items have metadata, since there's no way to construct new ones...
        String shieldName = customItemRecipes.extractCustomID(itemMeta);

        Shield shield = configManager.createShield(shieldName, true);
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
