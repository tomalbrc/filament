# Blocks

## File

Block configuration files are to be placed in `MyDatapack/data/<namespace>/filament/block/myblock.json` 

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
    "model": "mynamespace:custom/block/stone/pebbles_item"
  },
  "type": "count",
  "blockModelType": "biome_plant_block",
  "properties": {
    "destroyTime": 0,
    "blockBase": "minecraft:stone",
    "itemBase": "minecraft:paper"
  }
}
```

