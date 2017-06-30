package to.us.tf.AbsorptionShields.shield;

public class Shield
{
    private String name;
    private float maxShieldStrength;
    private long regenTime;
    private float regenRate;

    private long regenCounter;

    /**
     * @param strength maximum healthpoints of shield
     * @param time Time before regenerating in ticks
     * @param rate How many healthpoints to regenerate per second
     */
    public Shield(float strength, long time, float rate)
    {
        this.maxShieldStrength = strength;
        this.regenTime = time;
        this.regenRate = rate;

        this.regenCounter = time;
    }

    public void resetRegenCounter()
    {
        regenCounter = 0L;
    }

    /**
     *
     * @param ticks
     * @return if shield is allowed to regenerate
     */
    public boolean incrementCounter(long ticks)
    {
        if (regenCounter < regenTime)
            regenCounter += ticks;
        return regenCounter >= regenTime;
    }

    public float getMaxShieldStrength()
    {
        return maxShieldStrength;
    }

    public float getRegenRate()
    {
        return regenRate;
    }

    public String getName()
    {
        return name;
    }
}
