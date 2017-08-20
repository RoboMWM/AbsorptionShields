package to.us.tf.absorptionshields;

import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import to.us.tf.absorptionshields.command.AddLoreCommand;
import to.us.tf.absorptionshields.command.ConvertShieldCommand;
import to.us.tf.absorptionshields.shield.Shield;
import to.us.tf.absorptionshields.shield.ShieldManager;
import to.us.tf.absorptionshields.shield.ShieldUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
        getCommand("addlore").setExecutor(new AddLoreCommand(this));
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

    public ItemStack convertToShield(String shieldName, ItemStack itemStack)
    {
        if (!configManager.isValidShieldName(shieldName, false))
            return null;

        Shield shield = configManager.createShield(shieldName, false);
        ItemMeta itemMeta = itemStack.getItemMeta(); //I guess all items have metadata, since there's no way to construct new ones...
        itemMeta.setDisplayName(shield.getName());
        itemMeta.setLore(getStats(shield));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public List<String> getStats(Shield shield)
    {
        List<String> lore = new ArrayList<>();
        DecimalFormat df = new DecimalFormat("#.##");

        //TODO: configurable...
        lore.add("");
        lore.add(ChatColor.YELLOW + "AbsorptionShield Stats:");
        lore.add(ChatColor.GOLD + "- Capacity: " + ChatColor.YELLOW + shield.getMaxShieldStrength() / 2);
        lore.add(ChatColor.GOLD + "- Recharge Rate: " + ChatColor.YELLOW + shield.getRegenRate());
        lore.add(ChatColor.GOLD + "- Recharge Delay: " + ChatColor.YELLOW + df.format(shield.getRegenTime() / 20L));

        return lore;
    }

    public ItemStack addLore(String loreToAdd, ItemStack itemStack)
    {
        ItemMeta itemMeta = itemStack.getItemMeta(); //I guess all items have metadata, since there's no way to construct new ones...
        String shieldName = itemMeta.getDisplayName();

        if (!configManager.isValidShieldName(shieldName, true))
            return null;

        Shield shield = configManager.createShield(shieldName, true);

        List<String> lore = new ArrayList<>();

        loreToAdd = WordUtils.wrap(ChatColor.translateAlternateColorCodes('&', loreToAdd), 50, "\n", false);
        String[] loreToAddArray = loreToAdd.split("\n");
        lore.addAll(Arrays.asList(loreToAddArray));

        //Append stats at end
        lore.addAll(getStats(shield));

        itemMeta.setLore(lore);
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
