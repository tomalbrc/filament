# Item Behaviours

Item behaviours define specific functionalities associated with items, blocks, and decorations. 

All behaviours are optional, and some are mutually exclusive (e.g., trap, shoot, and instrument).

Example with some behaviours set:
```json
{
  "id": "mynamespace:multi_example",
  "vanillaItem": "minecraft:paper",
  "itemResource": {
    "models": {
      "default": "mynamespace:custom/misc/clown_horn"
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

---

### `armor` behaviour

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

- **Fields**:
  - `slot`: The equipment slot for the armour piece (e.g., head, chest, legs, or feet).
  - `texture`: The resource location of the texture associated with the armour. Example: `mynamespace:amethyst`
  - `trim`: Flag wether to use trim-based armor instead of shaders

---

### `compostable` behaviour

Makes the item usable in composters.

- **Fields**:
  - `chance`: Chance of raising the composter level by 1 between 0 and 100

---

### `cosmetic` behaviour

Defines cosmetic item behaviour for either the head or chestplate slot, supporting both Blockbench models for chestplates and simple item models for either slot.

- **Fields**:
  - `slot`: The equipment slot for the cosmetic (head or chest).
  - `model`: Optional, the resource location of the animated blockbench or animated-java model for the cosmetic.
  - `autoplay`: Optional, the name of the animation to autoplay, which should be loopable.
  - `scale`: Scale of the chest cosmetic, defaulting to (1, 1, 1).
  - `translation`: Translation of the chest cosmetic, defaulting to (0, 0, 0).

---

### `execute` behaviour

Executes a command on item use.

- **Fields**:
  - `consumes`: Indicates whether the execution consumes the item.
  - `command`: The command string to execute.
  - `sound`: Optional sound effect to play during execution.

---

### `food` behaviour

Defines food item behaviour for edible items.

- **Fields**:
  - `hunger`: The amount of hunger restored when consumed.
  - `saturation`: The saturation modifier provided by the food.
  - `canAlwaysEat`: Indicates whether the item can be eaten when the hunger bar is full.
  - `fastfood`: Boolean indicating whether the food item is considered fast food (eats faster than normal).

---

### `fuel` behaviour

Defines fuel behaviour for items, specifying their value used in furnaces and similar item-burning blocks.

- **Fields**:
  - `value`: The value associated with the fuel, determining burn duration.

---

### `instrument` behaviour

Defines instrument behaviour for items, similar to goat horns.

- **Fields**:
  - `sound`: The sound associated with the instrument.
  - `range`: The range of the instrument.
  - `useDuration`: Delay in ticks for using the instrument.

---

### `shoot` behaviour

Defines behaviour for items capable of shooting projectiles or being shot themselves.

- **Fields**:
  - `consumes`: Indicates whether shooting consumes the item.
  - `baseDamage`: The base damage of the projectile.
  - `speed`: The speed at which the projectile is fired.
  - `projectile`: The identifier for the projectile item.
  - `sound`: Optional sound effect to play when shooting.

---

### `stripper` behaviour

Let's the item strip Logs/scrape copper blocks like an axe. Uses 1 durability.

---

### `trap` behaviour

Defines trap behaviour for items capable of trapping specific entity types.

- **Fields**:
  - `types`: List of allowed entity types to trap. Example: `["minecraft:silverfish", "minecraft:spider"]`
  - `requiredEffects`: List of required effects for the trap. Example: `["minecraft:weakness"]`
  - `chance`: Chance of the trap triggering (`0`-`100`).
  - `useDuration`: Use cooldown for the trap item.
