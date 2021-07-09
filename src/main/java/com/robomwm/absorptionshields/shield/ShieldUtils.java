package com.robomwm.absorptionshields.shield;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created on 6/30/2017.
 * This is leftover back when reflection was required to get access to this field.
 * Now we don't need to do that anymore, yay!
 *
 * @author RoboMWM
 */

@Deprecated
public class ShieldUtils
{
    @Deprecated
    public double getShieldHealth(Player player)
    {
            return player.getAbsorptionAmount();
    }

    @Deprecated
    public void setShieldHealth(Player player, double points)
    {
            player.setAbsorptionAmount(points);
    }
}