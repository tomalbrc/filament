# Block Types

The following block types are supported, each providing a custom appearance and functionality based on block states:

### `block` (Default)
- **Model Key**: "default"
- **Description**: Uses only the "default" model key.

### `column`
- **Model Keys**: "x", "y", and "z"
- **Description**: Works like logs, pillars, or columns. Can be rotated to align with the X, Y, or Z axis.

### `count`
- **Model Keys**: "0" to "n"
- **Description**: The count block type can increase with each placement using the same item of the block. The count can be extended to any desired value.

### `slab`
- **Model Keys**: "top", "bottom", "double"
- **Description**: Waterloggable slab block

### `powerlevel`
- **Model Keys**: "0" to "15"
- **Description**: Can receive redstone power and change its model based on the incoming power level. The model is chosen based on the power level the block receives.

### `powered_directional`
- **Model Key Format**: `[north|south|east|west],powered=[true|false]`
- **Description**: The model key includes both direction (north, south, east, west) and power status (true or false). This is mainly used with the `repeater` block behaviour.



Example:
```
{
  "id": "mynamespace:custom_slab",
  "blockResource": {
    "models" : {
      "top": "minecraft:custom/block/dirt/top",
      "bottom": "minecraft:custom/block/dirt/bottom",
      "double" : "minecraft:custom/block/dirt/double"
    }
  },
  "type": "slab",
  "blockModelType": "slab_block",
  "properties": {
    "destroyTime": 20,
    "blockBase": "minecraft:dandelion",
    "itemBase": "minecraft:paper"
  }
}

```