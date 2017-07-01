package to.us.tf.absorptionshields.shield;

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
    public Shield(String name, float strength, long time, float rate)
    {
        this.name = name;
        this.maxShieldStrength = strength;
        this.regenTime = time;
        this.regenRate = rate;

        this.regenCounter = time;
    }

    /**
     * Copy constructor
     * @param template
     */
    public Shield(Shield template)
    {
        this.name = template.name;
        this.maxShieldStrength = template.maxShieldStrength;
        this.regenTime = template.regenTime;
        this.regenRate = template.regenRate;
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
