Support this project on [Patreon!](https://patreon.com/RoboMWM)

# AbsorptionShields
Have you ever dreamed of a day where the "personal shield" mechanic from games like Borderlands, Halo, Overwatch, etc. were in Minecraft? No? Well I did. Thanks to the absorption health being an actual, editable field instead of just being chained to a potion, this is possible to do somewhat rather elegantly in Minecraft.

#### Dependencies
- <a href="https://dev.bukkit.org/projects/customitemrecipes" target="_blank">CustomItemRecipes</a>

If you want, you can define just material types (using the Material enum name) to be used as a shield, so this dependency is technically optional. 

However, if you want multiple items of the same type to represent different kinds of shield (e.g. differentiated via lore, display name, etc.) you will need to use CustomItemRecipes to register these custom items for use in AbsorptionShields. You can also use that plugin to create the custom items too!

## A video is worth 60,000 words a second
Note: shields can now be any part of armor you choose (but must be consistent, e.g. if you choose the chestplate to be checked for shields, all shields must be chestplate items). Also, /createshield has been removed.

[Click to watch](https://www.youtube.com/watch?v=0GUw6ehtFXo)

[![AbsortionShields demonstration video](http://img.youtube.com/vi/0GUw6ehtFXo/0.jpg)](https://www.youtube.com/watch?v=0GUw6ehtFXo)

You can also test it out on MLG Fortress. IP: `MLG.ROBOMWM.COM`

## Features

- Create an unlimited number of shields (well, the limit being how many names you can come up with).
- Shield size, recharge rate and delay are all configurable.
- Shield recharges after not being hit.
- Shields are "on top" of the armor, protecting armor from being damaged when active.
- Players who take shield damage glow instead of just turning red momentarily.
- Integration with [MLG Damage Indicators](https://www.spigotmc.org/resources/mlg-damage-indicators.43438/)
  - Shield damage appears in orange instead of red.


### Commands
- `/addshieldstats <shield name>` - Appends lore with the specified shield stats to the held shield item. Particularly useful when creating a custom item to register in CustomItemRecipes. Requires the `absorptionshields.addshieldstats` permission, given to ops by default.

### Config.yml
```yml
Shields:
  'bumblebee':
    strength: 12 # Shield's max capacity in hearts
    time: 2 # Seconds before shield starts regenerating
    rate: 3 # How fast the shield recharges in hearts per second
  'sponge':
    strength: 24
    time: 5
    rate: 1
  'GOLDEN_HELMET': # You can use the Material enum name to allow unregistered custom items be a shield.
    strength: 18
    time: 3
    rate: 2
    
Sounds: # If you use a resource pack you can set custom sounds here. You could also experiment with vanilla sounds as well!
  shieldHitSelf: fortress.shieldhitself # taking damage
  lowShieldHitSelf: fortress.lowshieldhitself # taking damage when shield is at 1/3 or lower capacity
  shieldOfflineAlert: fortress.shieldoffline # Plays when shield is broken (the "beep beep beep" alert). Doesn't play if player manually unequips shield
  shieldBroken: fortress.shieldbroken #  shield shattering/breaking
  shieldBootingUp: fortress.shieldbootingup # Shield regenerating from empty.
  shieldFullyRecharged: fortress.shieldrecharged # Shield has fully regenerated.
  
armorSlotToCheck: HELMET # The armor slot you use to equip AbsorptionShields. HELMET, CHESTPLATE, LEGGINGS, or BOOTS

```
I hope you enjoy this plugin as much as I did in creating and using it!
