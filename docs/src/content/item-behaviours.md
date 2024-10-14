# Item Behaviours

Item behaviours define specific functionalities associated with items, blocks, and decorations. 

All behaviours are optional, and some are mutually exclusive (e.g., trap, shoot, and instrument).

Example with some behaviours set:
<details>
<summary>Click to expand</summary>

~~~admonish example
```json
{
  "id": "mynamespace:multi_example",
  "vanillaItem": "minecraft:paper",
  "itemResource": {
    "models": {
      "default": "mynamespace:custom/misc/clown_horn",
      "trapped": "mynamespace:custom/misc/clown_horn_trapped"
    }
  },
  "behaviour": {
    "instrument": {
      "sound": "mynamespace:misc.honk",
      "range": 64,
      "useDuration": 60
    },
    "shoot": {
      "consumes": false,
      "baseDamage": 2.0,
      "speed": 1.0,
      "projectile": "minecraft:iron_axe",
      "sound": "mynamespace:misc.shoot"
    },
    "armor": {
      "slot": "head",
      "texture": "mynamespace:texture_name"
    },
    "trap": {
      "types": ["minecraft:villager", "minecraft:zombie", "minecraft:skeleton"],
      "requiredEffects": ["minecraft:glowing"],
      "chance": 75,
      "useDuration": 0
    },
    "fuel": {
      "value": 10
    },
    "food": {
      "hunger": 2,
      "saturation": 1.0,
      "canAlwaysEat": true,
      "fastfood": true
    },
    "cosmetic": {
      "slot": "head",
      "model": "mynamespace:custom/models/clown_backpack_animated",
      "autoplay": "idle",
      "scale": [1.5, 1.5, 1.5],
      "translation": [0.0, 0.5, 0.0]
    },
    "execute": {
      "consumes": true,
      "command": "/summon minecraft:creeper ~ ~ ~ {powered:1b}",
      "sound": "minecraft:block.anvil.place"
    }
  }
}
```
~~~
</details>

---

## `armor` behaviour
<img class="right" src="../img/armor.png" alt="armor">

Defines armour item behaviours, utilizing the Fancypants shader via Polymer.

As of filament 0.7 it is possible to use armor trims to render custom armor, to enable this, set the `trim` flag to `true`.

**For Shader-Based Armor**:

- The textures for the shader version of armor should be located in `assets/minecraft/textures/models/armor/`.
- For a `texture` field value of `mynamespace:amethyst`, the textures should be named `amethyst_layer_1.png` and `amethyst_layer_2.png`.
- It's required to use a leather armor item for your `vanillaItem` for the shader to work.

**For Trim-Based Armor**:

- When using Armor Trims for the armor, the textures need to be located in `assets/minecraft/textures/trims/models/armor/`.
- For a `texture` field value of `mynamespace:amethyst`, the textures should be named `amethyst.png` and `amethyst_leggings.png`.
- It's required to use an armor item for your `vanillaItem`. Any armor item should work.
- Depending on the `vanillaItem` of your custom item, you might be able to see parts of the original armors texture, to mitigate this, you will have to enable the `trimArmorReplaceChainmail` option in the mods configs.
- Enabling `trimArmorReplaceChainmail` will prevent all chainmail armor pieces unable to receive or display armor trims. The Smithing Table will also reject chainmail armor with this option enabled.

### Fields:
- `slot`: The equipment slot for the armour piece (e.g., head, chest, legs, or feet).
- `texture`: The resource location of the texture associated with the armour. Example: `mynamespace:amethyst`
- `trim`: Flag wether to use trim-based armor instead of shaders

<br>
<br>

---

## `compostable` behaviour
<img class="right" src="../img/compostable.png" alt="compostable">

Makes the item usable in composters.

### Fields:
- `chance`: Chance of raising the composter level by 1 between 0 and 100
- `villagerInteraction`: Allows farmer villagers to compost the item. Defaults to `true`

<br>
<br>

---

## `cosmetic` behaviour
<img class="right" src="../img/cosmetic.png" alt="cosmetic">

Defines cosmetic item behaviour for either the head or chestplate slot, supporting both Blockbench models for chestplates and simple item models for either slot.

### Fields:
- `slot`: The equipment slot for the cosmetic (head or chest).
- `model`: Optional, the resource location of the animated blockbench or animated-java model for the cosmetic.
- `autoplay`: Optional, the name of the animation to autoplay, which should be loopable.
- `scale`: Scale of the chest cosmetic, defaulting to (1, 1, 1).
- `translation`: Translation of the chest cosmetic, defaulting to (0, 0, 0).

<br>
<br>

---

## `execute` behaviour

Executes a command on item use with the player as source, located at the player.

### Fields:
- `consumes`: Indicates whether the execution consumes the item. Defaults to `false`
- `command`: The command string to execute. Empty by default
- `sound`: Optional sound effect to play during execution. Empty by default

<br>
<br>

---

## `food` behaviour
<img class="right" src="../img/food.png" alt="food">

Defines food item behaviour for edible items.

### Fields:
- `hunger`: The amount of hunger restored when consumed. Defaults to `1`
- `saturation`: The saturation modifier provided by the food. Defaults to `0.6`
- `canAlwaysEat`: Indicates whether the item can be eaten when the hunger bar is full. Defaults to `false` 
- `fastfood`: Boolean indicating whether the food item is considered fast food (eats faster than normal). Defaults to `false`

<br>
<br>

---

## `villager_food` behaviour
<img class="right" src="../img/villager_food.png" alt="villager_food">

Makes the item edible for villagers (for villager breeding).

### Fields:
  - `value`: The amount of "breeding power" the item has (1 = normal food item, 4 = bread). Defaults to `1`

<br>
<br>

---

## `fuel` behaviour

Defines fuel behaviour for items, specifying their value used in furnaces and similar item-burning blocks.

- **Fields**:
  - `value`: The value associated with the fuel, determining burn duration. Defaults to `10`

<br>
<br>

---

## `instrument` behaviour
<img class="right" src="../img/instrument.png" alt="instrument">

Defines instrument behaviour for items, similar to goat horns.

- **Fields**:
  - `sound`: The sound associated with the instrument. Empty by default
  - `range`: The range of the instrument. Defaults to `0`
  - `useDuration`: Delay in ticks for using the instrument. Defaults to `0`

<br>
<br>

---

## `stripper` behaviour

Gives the item the ability to strip Logs/scrape copper blocks, like an axe. Uses 1 durability.

<br>

---

## `trap` behaviour
<img class="right" src="../img/trap.png" alt="trap">

Defines trap behaviour for items capable of trapping specific entity types.

- **Fields**:
  - `types`: List of allowed entity types to trap. Example: `["minecraft:silverfish", "minecraft:spider"]`
  - `requiredEffects`: List of required effects for the trap. Example: `["minecraft:weakness"]`
  - `chance`: Chance of the trap triggering (`0`-`100`). Defaults to `50`
  - `useDuration`: Use cooldown for the trap item. Defaults to `0`

<br>
<br>

---

## `banner_pattern` behaviour
<img class="right" src="../img/banner_pattern.gif" alt="banner pattern">

Allows you to assign a banner pattern to an item for use in Looms.

See the `mynamespace:bannertestitem` item config in the example datapack in the GitHub repo.

- **Fields**:
  - `id`: The id of your banner_pattern in your datapack. Empty by default

<br>
<br>
<br>

---

## `bow` behaviour
<img class="right" src="../img/bow.png" alt="bow">

Vanilla-like bow behaviour. Lets you specify which item can be shot, but anything that is not an arrow or firework rocket will render as normal arrow.
Allows to specify a power multiplier for shooting power. Supports firework rockets.

**Make sure to use `minecraft:bow` as `vanillaItem` in order for the item model overrides to work properly!**


- **Fields**:
  - `powerMultiplier`: The power multiplier. Defaults to `3`
  - `supportedProjectiles`: List of supported items in the inventory for use with the bow. Defaults to `["minecraft:arrow", "minecraft:spectral_arrow"]`
  - `supportedHeldProjectiles`: List of supported items for use when in main/offhand. Defaults to `["minecraft:arrow", "minecraft:spectral_arrow", "minecraft:firework_rocket"]`
  - `shootSound`: The sound when shooting a projectile. Default to `minecraft:entity.arrow.shoot`

<br>
<br>

---

## `crossbow` behaviour
<img class="right" src="../img/crossbow.png" alt="crossbow">

Vanilla-like crossbow behaviour. Lets you specify which item can be shot, but anything that is not an arrow or firework rocket will render as normal arrow.
Allows to specify a power multiplier for shooting power.

**Make sure to use `minecraft:crossbow` as `vanillaItem` in order for the item model overrides to work properly!**


### Fields:
- `powerMultiplier`: The power multiplier. Defaults to `1`
- `supportedProjectiles`: List of supported items in the inventory for use with the crossbow. Defaults to `["minecraft:arrow", "minecraft:spectral_arrow"]`
- `supportedHeldProjectiles`: List of supported items for use when in main/offhand. Defaults to `["minecraft:arrow", "minecraft:spectral_arrow", "minecraft:firework_rocket"]`
- `shootSound`: The sound when shooting a projectile. Default to `minecraft:item.crossbow.shoot`
- `loadingStartSound`: Projectile loading start sound. Default to `minecraft:item.crossbow.loading_start`
- `loadingMiddleSound`: Projectile loading middle sound. Default to `minecraft:item.crossbow.loading_middle`
- `loadingEndSound`: Projectile loading end sound. Default to `minecraft:item.crossbow.loading_end`

<br>
<br>

---

## `shoot` behaviour

Defines behaviour for items capable of shooting custom projectiles or being shot themselves.

### Fields:
- `consumes`: Indicates whether shooting consumes the item. Defaults to `false`
- `baseDamage`: The base damage of the projectile. Defaults to `2.0`
- `speed`: The speed at which the projectile is fired. Defaults to `1.0`
- `projectile`: The identifier for the projectile item. Empty by default
- `sound`: Optional sound effect to play when shooting. Empty by default
- `translation`: Translation offset for the projectile. Defaults to `[0 0 0]`
- `rotation`: Rotation for the projectile. Defaults to `[0 90 0]`
- `scale`: Scale for the projectile. Defaults to `0.6`
