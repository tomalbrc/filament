# Block Properties

Block properties share the same properties as [items](item-properties.md)

All properties (excluding the shared item and decoration properties):
```json
{
  "properties": {
    "blockBase": "minecraft:stone",
    "itemBase": "minecraft:paper",
    "requiresTool": true,
    "explosionResistance": 10,
    "destroyTime": 5,
    "redstoneConductor": false,
    "lightEmission": 0
  }
}
```

### `blockBase`:

Specifies the base block for this block property. Must be a valid block identifier.
This is used for sounds and particles.

Defaults to `minecraft:stone`

### `requiresTool`:

Boolean (true/false) indicating whether the block requires a specific tool to be harvested.

Defaults to `true`

### `explosionResistance`:

Number indicating the block's resistance to explosions.

Defaults to `0`

### `destroyTime`:

Resistance of the block/the time required to destroy the block.

The destroyTime is used as explosionResistance if explosionResistance is not explicitly specified.

Defaults to `0`

### `redstoneConductor`:

Boolean (true/false) indicating whether the block can conduct redstone signals.

Defaults to `true`

### `lightEmission`:

Light level this block emits.

Defaults to `0`

### `transparent`:
Flag indicating whether the block is transparent. Transparent blocks don't block light

Defaults to `false`

### `allowsSpawning`:
Flag indicating whether mobs can spawn on this block.

Defaults to `false`

### `replaceable`:
Flag indicating whether this block can be replaced by another block when placing a new block (e.g., grass can be replaced when placing a solid block).
Defaults to `false`

### `collision`:
Flag indicating whether the block has collision
Defaults to `true`

### `solid`:
Flag indicating whether the block gets flushed away with water.

Defaults to `true`

### `pushReaction`:
Specifies how the block reacts to being pushed by a piston. Possible values include normal, destroy, block

Defaults to `normal`