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

Defaults to `minecraft:stone`

### `itemBase`:

Specifies the base item for this block property. Must be a valid item identifier.

Defaults to `minecraft:paper`

### `requiresTool`:

Boolean (true/false) indicating whether the block requires a specific tool to be harvested.

Defaults to `false`

### `explosionResistance`:

Number indicating the block's resistance to explosions.

### `destroyTime`:

Time in seconds indicating the time required to destroy the block.

### `redstoneConductor`:

Boolean (true/false) indicating whether the block can conduct redstone signals.

Defaults to `true`

### `lightEmission`:

Light level this block emits.

Defaults to `0`
