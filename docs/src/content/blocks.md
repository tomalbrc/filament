# Blocks

## File

Block configuration files are to be placed in `MyDatapack/data/<namespace>/filament/block/myblock.json` 

Most Item behaviours such as `food`, `fuel`, `cosmetic` and more are supported by blocks.

You can also set components for the block-item, similar to item configurations using the `components` field

## Contents

Blocks like candles or turtle eggs, with mutiple blocks in 1:
```json
{
  "id": "mynamespace:pebbles",
  "vanillaItem": "minecraft:paper",
  "blockResource": {
    "models": {
      "count=1": "mynamespace:custom/block/stone/pebbles_1",
      "count=2": "mynamespace:custom/block/stone/pebbles_2",
      "count=3": "mynamespace:custom/block/stone/pebbles_3",
      "count=4": "mynamespace:custom/block/stone/pebbles_4"
    }
  },
  "itemResource": {
    "models": {
      "default": "mynamespace:custom/block/stone/pebbles_item"
    }
  },  
  "blockModelType": "biome_plant_block",
  "properties": {
    "destroyTime": 0,
    "blockBase": "minecraft:stone"
  },
  "components": {
    ...
  }
}
```

The fields `id`, `blockResource`, and `blockModelType` are required to be set.

# Fields:

## `id` (required): 
Identifier of the block and its item.

Example: `mynamespace:myblock`

## `vanillaItem`:

The vanilla item that is sent to the client and gets skinned using CustomModelData internally.
Defaults to `minecraft:paper`

## `blockResource` (required):

An object that allows you to provide different block models for each block-state-property that may be provided by a block behaviour.

For now it is only possible to provide block models directly, support for just textures is planned for a future version.

The keys work similar to the vanilla blockstate files in resourcepacks, you specify the model to use based on the block-state.

An example for the `count` block-behaviour:
```
{
  ...,
  "blockResource": {
    "models": {
      "count=1": "mynamespace:custom/block/stone/pebbles_1",
      "count=2": "mynamespace:custom/block/stone/pebbles_2",
      "count=3": "mynamespace:custom/block/stone/pebbles_3",
      "count=4": "mynamespace:custom/block/stone/pebbles_4"
    }
  },
  ...
}
```

## ```itemResource```:

An object that allows you provide different item-models which may be required by some item-behaviours.

The `trapped` behaviour for example requires a model for the `trapped` key.

Example
```
{
  ...,
  "itemResource": {
    "models": {
      "default": "mynamespace:custom/trap/allay_trap"
      "trapped": "mynamespace:custom/trap/allay_trap_filled"
    }
  },
  ...
}

```

## `blockModelType` (required in most cases):

The block model to use/retexture. See [Block Model Types](block-model-types.md) for a list of options.

***For some block behaviours like `slab` it may be required to leave this field empty!***

## `properties`: 

The properties of this block. See [Block Properties](block-properties.md) for details.

## `components`:

An object with minecraft components used for the item.

Example:
```
{
    ...
    "components": {
    "minecraft:tool": {
      "default_mining_speed":1.0,
      "damage_per_block":2,
      "rules":[
        {
          "speed":15,
          "correct_for_drops":true,
          "blocks":"#sword_efficient"
        },
        {
          "speed":1.5,
          "blocks":"cobweb"
        }
      ]
    }
  },
  ...
}
```