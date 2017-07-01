package to.us.tf.AbsorptionShields;

import to.us.tf.AbsorptionShields.shield.Shield;

/**
 * Created on 6/30/2017.
 *
 * @author RoboMWM
 */
public class ConfigManager
{
    public boolean isValidShieldName(String name)
    {
        return true;
    }

    public Shield createShield(String name)
    {
        return new Shield();
    }
}
