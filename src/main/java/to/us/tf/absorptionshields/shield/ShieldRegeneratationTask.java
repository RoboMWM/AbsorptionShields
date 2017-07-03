package to.us.tf.absorptionshields.shield;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created on 6/30/2017.
 * Shield regeneration task
 *
 * Initially I was going to stick this in the Shield object but
 * a) would have to cancel task when object is "deleted" and
 * b) would have to pass a plugin instance to each.
 *
 * @author RoboMWM
 */
public class ShieldRegeneratationTask extends BukkitRunnable
{
    ShieldManager shieldManager;
    ShieldUtils shieldUtils;
    long rateToCheck;

    ShieldRegeneratationTask(ShieldManager shieldManager, ShieldUtils shieldUtils, long rate)
    {
        this.shieldManager = shieldManager;
        this.shieldUtils = shieldUtils;
        this.rateToCheck = rate;
    }

    @Override
    public void run()
    {
        for (Player player : shieldManager.getPlayersWithDamagedShields())
        {
            //No longer has an active shield
            if (!shieldManager.hasShield(player))
            {
                shieldManager.addPlayerWithDamagedShield(player);
                continue;
            }

            Shield shield = shieldManager.getShield(player);

            //Not ready to regenerate yet
            if (!shield.incrementCounter(5L))
                continue;

            float shieldHealth = shieldUtils.getShieldHealth(player);

            //All regenerated
            if (shieldHealth >= shield.getMaxShieldStrength())
            {
                shieldUtils.setShieldHealth(player, shield.getMaxShieldStrength());
                shieldManager.removePlayerWithDamagedShield(player);
                //TODO: shield regeneration complete sound
                continue;
            }

            //Regen
            if (shieldHealth < shield.getMaxShieldStrength())
            {
                float amountToRegen = shield.getRegenRate() / (20L / rateToCheck);
                float missingShield = shield.getMaxShieldStrength() - shieldHealth;
                //TODO: regeneration sound effect with pitch on missingShield.
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_CHIME, 0.5f, 0.5f + (missingShield / (shield.getMaxShieldStrength() / 1.5f)));

                //TODO: sound effect when starting regeneration of fully depleted shield
                if (shieldHealth <= 0)
                    player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 0.1f, 2f);

                //Top off if near full
                if (amountToRegen > missingShield)
                {
                    shieldUtils.setShieldHealth(player, shield.getMaxShieldStrength());
                }
                else
                    shieldUtils.setShieldHealth(player, shieldHealth + amountToRegen);
            }
        }
    }
}
