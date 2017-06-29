package to.us.tf.AbsorptionShields.listeners;

import net.minecraft.server.v1_11_R1.EntityLiving;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.metadata.FixedMetadataValue;
import to.us.tf.AbsorptionShields.AbsorptionShields;

/**
 * Created on 3/18/2017.
 *
 * @author RoboMWM
 *
 * Uses NMS.
 * If you want to convert this to CB-only code, you will have to manually track adding and damage to absorption hearts
 * In other words, I'm not going to waste my time developing a system to do that.
 */
public class DamageListener implements Listener
{
    AbsorptionShields instance;


    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onPlayerDamage(EntityDamageEvent event)
    {
        if (event.getEntityType() != EntityType.PLAYER)
            return;
        Player player = (Player)event.getEntity();
        player.setMetadata("AS_LASTHIT", new FixedMetadataValue(instance, System.currentTimeMillis()));

        //Check if wearing shield (metadata?)

        CraftPlayer craftPlayer = (CraftPlayer)event.getEntity();

        EntityLiving nmsPlayer = craftPlayer.getHandle();

        final float originalShieldHealth = nmsPlayer.getAbsorptionHearts();

        if (originalShieldHealth == 0)
            return;
        float shieldHealth = originalShieldHealth;
        Bukkit.broadcastMessage("shieldHealth: " + String.valueOf(originalShieldHealth));
        double armorDamage = event.getOriginalDamage(EntityDamageEvent.DamageModifier.ARMOR);

        shieldHealth += armorDamage; //armordamage is negative

        if (shieldHealth < 0)
        {
            event.setDamage(EntityDamageEvent.DamageModifier.ARMOR, shieldHealth);
            event.setDamage(EntityDamageEvent.DamageModifier.ABSORPTION, -originalShieldHealth);
            return;
        }

        event.setDamage(EntityDamageEvent.DamageModifier.ARMOR, -0.0); //Spottedleaf
        event.setDamage(EntityDamageEvent.DamageModifier.ABSORPTION, event.getOriginalDamage(EntityDamageEvent.DamageModifier.ABSORPTION) + (shieldHealth - originalShieldHealth));
    }

    //Reset shield regen on respawn

    double damageToBeDealt(float shield, double damage)
    {

    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    private void onPlayerArmorDamage(PlayerItemDamageEvent event)
    {

    }
}
