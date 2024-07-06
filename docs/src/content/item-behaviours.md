Certainly! Here's the updated documentation for item behaviours along with the descriptions of the internal classes provided:

# Behaviours

Item behaviours define specific functionalities associated with items, blocks, and decorations. All behaviours are optional, and some are mutually exclusive (e.g., trap, shoot, and instrument).

Example with all behaviours set:
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

### `armor` behaviour

**Description**:
Defines armour item behaviours, utilizing the Fancypants shader via Polymer.

- **Fields**:
  - `slot`: The equipment slot for the armour piece (e.g., head, chest, legs, or feet).
  - `texture`: The resource location of the texture associated with the armour.

### `cosmetic` behaviour

**Description**:
Defines cosmetic item behaviours for either the head or chestplate slot, supporting both Blockbench models for chestplates and simple item models for either slot.

- **Fields**:
  - `slot`: The equipment slot for the cosmetic (head or chest).
  - `model`: Optional, the resource location of the animated blockbench or animated-java model for the cosmetic.
  - `autoplay`: Optional, the name of the animation to autoplay, which should be loopable.
  - `scale`: Scale of the chest cosmetic, defaulting to (1, 1, 1).
  - `translation`: Translation of the chest cosmetic, defaulting to (0, 0, 0).

### `execute` behaviour

**Description**:
Executes a command on item use.

- **Fields**:
  - `consumes`: Indicates whether the execution consumes the item.
  - `command`: The command string to execute.
  - `sound`: Optional sound effect to play during execution.

### `food` behaviour

**Description**:
Defines food item behaviours for edible items.

- **Fields**:
  - `hunger`: The amount of hunger restored when consumed.
  - `saturation`: The saturation modifier provided by the food.
  - `canAlwaysEat`: Indicates whether the item can be eaten when the hunger bar is full.
  - `fastfood`: Boolean indicating whether the food item is considered fast food (eats faster than normal).

### `fuel` behaviour

**Description**:
Defines fuel behaviours for items, specifying their value used in furnaces and similar item-burning blocks.

- **Fields**:
  - `value`: The value associated with the fuel, determining burn duration.

### `instrument` behaviour

**Description**:
Defines instrument behaviours for items, similar to goat horns.

- **Fields**:
  - `sound`: The sound associated with the instrument.
  - `range`: The range of the instrument.
  - `useDuration`: Delay in ticks for using the instrument.

### `shoot` behaviour

**Description**:
Defines behaviours for items capable of shooting projectiles or being shot themselves.

- **Fields**:
  - `consumes`: Indicates whether shooting consumes the item.
  - `baseDamage`: The base damage of the projectile.
  - `speed`: The speed at which the projectile is fired.
  - `projectile`: The identifier for the projectile item.
  - `sound`: Optional sound effect to play when shooting.

### `Trap` behaviour

**Description**:
Defines trap behaviours for items capable of trapping specific entity types.

- **Fields**:
  - `types`: List of allowed entity types to trap.
  - `requiredEffects`: List of required effects for the trap.
  - `chance`: Chance of the trap triggering (0-100).
  - `useDuration`: Delay in ticks for using the trap.
