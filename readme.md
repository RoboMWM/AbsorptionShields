Support this project on [Patreon!](https://patreon.com/RoboMWM)

# AbsorptionShields
Have you ever dreamed of a day where the "personal shield" mechanic from games like Borderlands, Halo, Overwatch, etc. were in Minecraft? No? Well I did. Thanks to the absorption health being an actual, editable field instead of just being chained to a potion, this is possible to do somewhat rather elegantly in Minecraft.

## A video is worth a 60,000 words a second
[Click to watch](https://www.youtube.com/watch?v=0GUw6ehtFXo)

[![AbsortionShields demonstration video](http://img.youtube.com/vi/0GUw6ehtFXo/0.jpg)](https://www.youtube.com/watch?v=0GUw6ehtFXo)

You can also test it out on MLG Fortress. IP: `MLG.ROBOMWM.COM`

## Features

- Create unlimited number of shields (well, the limit being how many names you can come up with).
- Shield size, recharge rate and delay are all configurable.
- Shield recharges after not being hit.
- Shield damage protects against health _and_ armor damage when active.
- Players who take shield damage glow instead of just turning red momentarily.
- Integration with [MLG Damage Indicators](https://www.spigotmc.org/resources/mlg-damage-indicators.43438/)
  - Shield damage appears in orange instead of red.


### Commands
- `/createshield <shield name>` - Converts a helmet held in the primary hand to an AbsorptionShield. Requires `absorptionshields.createshield` permission, given to ops by default.

### Config.yml
```yml
Shields:
  '&6bumblebee':
    strength: 12 # Shield's max capacity in hearts
    time: 2 # Seconds before shield starts regenerating
    rate: 3 # How fast the shield recharges in hearts per second
  '&eSponge':
    strength: 24
    time: 5
    rate: 1
    
Sounds: # If you use a resource pack you can set custom sounds here. You could also experiment with vanilla sounds as well!
  shieldHitSelf: fortress.shieldhitself # taking damage
  lowShieldHitSelf: fortress.lowshieldhitself # taking damage when shield is at 1/3 or lower capacity
  shieldOfflineAlert: fortress.shieldoffline # Plays when shield is broken (the "beep beep beep" alert). Doesn't play if player manually unequips shield
  shieldBroken: fortress.shieldbroken #  shield shattering/breaking
  shieldBootingUp: fortress.shieldbootingup # Shield regenerating from empty.
  shieldFullyRecharged: fortress.shieldrecharged # Shield has fully regenerated.

```
I hope you enjoy this plugin as much as I did in creating and using it!
