package com.robomwm.absorptionshields.event;

import com.robomwm.absorptionshields.shield.ShieldManager;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Created on 7/6/2017.
 *
 * Called when a player takes shield damage
 * Called in between EntityDamageEvent (high priority)
 *
 * @see ShieldManager
 * @author RoboMWM
 */
public class ShieldDamageEvent extends Event
{
    // Custom Event Requirements
    private static final HandlerList handlers = new HandlerList();
    public static HandlerList getHandlerList() {
        return handlers;
    }
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    private double damage;
    private Player victim;
    private EntityDamageEvent baseEvent;

    public ShieldDamageEvent(Player victim, double damage, EntityDamageEvent event)
    {
        this.damage = damage;
        this.victim = victim;
        this.baseEvent = event;
    }

    public double getDamage()
    {
        return damage;
    }

    public Player getVictim()
    {
        return victim;
    }

    public EntityDamageEvent getBaseEvent()
    {
        return baseEvent;
    }
}
