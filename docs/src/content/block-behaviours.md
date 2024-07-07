# Block Behaviours

Block behaviours can be customized to add unique properties to blocks. All behaviours are optional, and some can be applied to both items and blocks.

Example of a block with behaviours set:
```json
{
  "id": "mynamespace:myblock",
  "blockResource": {
    "models": {
      "default": "mynamespace:custom/block/myblock"
    }
  },
  "blockModelType": "full_block",
  "properties": {
    "destroyTime": 0,
    "blockBase": "minecraft:stone",
    "itemBase": "minecraft:paper"
  },
  "behaviour": {
    "powersource": {
      "value": 15
    },
    "repeater": {
      "delay": 1,
      "loss": 1
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
      "slot": "chest",
      "model": "mynamespace:custom/models/clown_backpack_animated",
      "autoplay": "idle",
      "scale": [1.5, 1.5, 1.5],
      "translation": [0.0, 0.5, 0.0]
    },
    "strippable": {
      "replacement": "minecraft:stone"
    }
  }
}
```

### `powersource` behaviour

**Description**:
Defines the block as a redstone power source.

- **Fields**:
    - `value`: The redstone power value the block emits.

### `repeater` behaviour

**Description**:
Defines the block as a redstone repeater with configurable delay and loss.

- **Fields**:
    - `delay`: Delay in ticks.
    - `loss`: Power loss during transfer.

### `fuel` behaviour

**Description**:
Defines fuel behaviours for blocks, specifying their value used in furnaces and similar item-burning blocks.

- **Fields**:
    - `value`: The value associated with the fuel, determining burn duration.

### `food` behaviour

**Description**:
Defines food item behaviours for edible blocks.

- **Fields**:
    - `hunger`: The amount of hunger restored when consumed.
    - `saturation`: The saturation modifier provided by the food.
    - `canAlwaysEat`: Indicates whether the item can be eaten when the hunger bar is full.
    - `fastfood`: Boolean indicating whether the food item is considered fast food (eats faster than normal).

### `cosmetic` behaviour

**Description**:
Defines cosmetic behaviours for blocks, supporting both Blockbench models for chestplates and simple item models.

- **Fields**:
    - `slot`: The equipment slot for the cosmetic (head or chest).
    - `model`: Optional, the resource location of the animated blockbench or animated-java model for the cosmetic.
    - `autoplay`: Optional, the name of the animation to autoplay, which should be loopable.
    - `scale`: Scale of the chest cosmetic, defaulting to (1, 1, 1).
    - `translation`: Translation of the chest cosmetic, defaulting to (0, 0, 0).

### `strippable` behaviour

**Description**:
Defines the block as strippable, replacing it with another block when interacted with an axe.

- **Fields**:
    - `replacement`: The identifier of the block to replace the current block with (e.g., "minecraft:stone").