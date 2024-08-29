# Blocks

## File

Block configuration files are to be placed in `MyDatapack/data/<namespace>/filament/block/myblock.json` 

Item behaviours such as `food`, `fuel` and `cosmetic` are supported by decorations.
You can also set components similar to item configurations using the `components` field

## Contents

Blocks like candles or turtle eggs, with mutiple blocks in 1:
```json
{
  "id": "mynamespace:pebbles",
  "blockResource": {
    "models": {
      "1": "mynamespace:custom/block/stone/pebbles_1",
      "2": "mynamespace:custom/block/stone/pebbles_2",
      "3": "mynamespace:custom/block/stone/pebbles_3",
      "4": "mynamespace:custom/block/stone/pebbles_4"
    }
  },
  "itemResource": {
    "models": {
      "default": "mynamespace:custom/block/stone/pebbles_item"
    }
  },  
  "type": "count",
  "blockModelType": "biome_plant_block",
  "properties": {
    "destroyTime": 0,
    "blockBase": "minecraft:stone",
    "itemBase": "minecraft:paper"
  },
  "components": {
    ...
  }
}
```

