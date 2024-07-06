# Block Properties

The `BlockProperties` JSON configuration defines various properties for a custom block. These properties include the base block and item, tool requirements, explosion resistance, destroy time, and redstone conductivity.

## Fields

### `blockBase`
- **Type**: `String`
- **Default Value**: `"minecraft:stone"`
- **Description**: Specifies the base block for this block property. Must be a valid block identifier.

### `itemBase`
- **Type**: `String`
- **Default Value**: `"minecraft:paper"`
- **Description**: Specifies the base item for this block property. Must be a valid item identifier.

### `requiresTool`
- **Type**: `boolean`
- **Default Value**: `false`
- **Description**: Indicates whether the block requires a specific tool to be harvested.

### `explosionResistance`
- **Type**: `number`
- **Default Value**: none
- **Description**: The block's resistance to explosions.

### `destroyTime`
- **Type**: `number`
- **Default Value**: none
- **Description**: The time required to destroy the block.

### `redstoneConductor`
- **Type**: `boolean`
- **Default Value**: `true`
- **Description**: Indicates whether the block can conduct redstone signals.

## Example

```json
"properties": {
  "blockBase": "minecraft:stone",
  "itemBase": "minecraft:paper",
  "requiresTool": true,
  "explosionResistance": 10,
  "destroyTime": 5,
  "redstoneConductor": false
}
```

This JSON configuration will create a block with the specified properties, such as requiring a tool for loot-drop, having specific explosion resistance and destroy time, and not conducting redstone signals.