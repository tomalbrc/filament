# Blocks

The misode based generator for blocks can be found [here!](https://misode.tomalbrc.de/filament/block)

## File

Block configuration files are to be placed in `MyDatapack/data/<namespace>/filament/block/myblock.json` 

Most Item behaviours such as `food`, `fuel`, `cosmetic` and more are supported by blocks.

You can also set components for the block-item, similar to item configurations using the `components` field

For World-Gen: Filament supports biome_modifications in datapacks, similar to forge. Checkout the example datapack in filaments GitHub repo!

---

## Contents

Blocks like candles or turtle eggs, with mutiple blocks in 1:
~~~admonish example
<!-- langtabs-start -->
```yml
id: mynamespace:pebbles
vanillaItem: minecraft:paper
itemTags:
  - minecraft:enchantable/durability # optional item tags
blockTags:
  - minecraft:dirt # optional block tags
blockResource:
  models:
    count=1: mynamespace:custom/block/stone/pebbles_1
    count=2: mynamespace:custom/block/stone/pebbles_2
    count=3: mynamespace:custom/block/stone/pebbles_3
    count=4: mynamespace:custom/block/stone/pebbles_4
itemResource:
  models:
    default: mynamespace:custom/block/stone/pebbles_item
blockModelType: biome_plant_block
properties:
  destroyTime: 0
  blockBase: minecraft:stone
group: mynamespace:myblockgroup
components:
  # ...
```

```json
{
  "id": "mynamespace:pebbles",
  "vanillaItem": "minecraft:paper",
  "itemTags": ["minecraft:enchantable/durability"], // optional item tags
  "blockTags": ["minecraft:dirt"], // optional block tags
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
  "group": "mynamespace:myblockgroup",
  "components": {
    // ...
  }
}
```
<!-- langtabs-end -->

~~~

The fields `id`, `blockResource`, and `blockModelType` are required to be set.

---

# Fields:

## `id` (required): 
Identifier of the block and its item.

~~~admonish example
Example: `mynamespace:myblock`
~~~

---

## `vanillaItem`:

The vanilla item that is sent to the client and gets skinned using CustomModelData internally.
Defaults to `minecraft:paper`

---

## `blockResource` (required):

An object that allows you to provide different block models for each block-state-property that may be provided by a block behaviour.

The keys work similar to the vanilla blockstate files in resourcepacks, you specify the model to use based on the block-state.

An example for the `count` block-behaviour:
~~~admonish example
<!-- langtabs-start -->
```yml
blockResource:
  models:
    count=1: mynamespace:custom/block/stone/pebbles_1
    count=2: mynamespace:custom/block/stone/pebbles_2
    count=3: mynamespace:custom/block/stone/pebbles_3
    count=4: mynamespace:custom/block/stone/pebbles_4
```

```json
{
  "blockResource": {
    "models": {
      "count=1": "mynamespace:custom/block/stone/pebbles_1",
      "count=2": "mynamespace:custom/block/stone/pebbles_2",
      "count=3": "mynamespace:custom/block/stone/pebbles_3",
      "count=4": "mynamespace:custom/block/stone/pebbles_4"
    }
  }
}
```
<!-- langtabs-end -->

~~~

Block models can be automatically generated from textures, like this:
~~~admonish example
<!-- langtabs-start -->
```yml
id: mynamespace:black_concrete_bricks
blockResource:
  parent: block/cube_all
  textures:
    default:
      all: minecraft:custom/block/concrete/black_concrete_bricks
properties:
  blockBase: minecraft:black_concrete
blockModelType: full_block
```

```json
{
  "id": "mynamespace:black_concrete_bricks",
  "blockResource": {
    "parent": "block/cube_all",
    "textures": {
      "default": {
        "all": "minecraft:custom/block/concrete/black_concrete_bricks"
      }
    }
  },
  "properties": {
    "blockBase": "minecraft:black_concrete"
  },
  "blockModelType": "full_block"
}
```
<!-- langtabs-end -->

~~~

---

## `itemModel`

Path to an item model definition (in `assets/namespace/items/<name>.json`)
This overwrites the itemResource field.

---

## `itemResource`:

An object that allows you to provide different item-models which may be required by some item-behaviours.

The `trapped` behaviour for example requires a model for the `trapped` key.

~~~admonish example
<!-- langtabs-start -->
```yml
itemResource:
  models:
    default: mynamespace:custom/block/model
```

```json
{
  "itemResource": {
    "models": {
      "default": "mynamespace:custom/block/model"
    }
  }
}
```
<!-- langtabs-end -->

~~~

The item model can also be auto-generated based on textures.

Example for automatic model generation based on textures:
~~~admonish example
<!-- langtabs-start -->
```yml
item_resource:
  parent: item/generated
  textures:
    default:
      layer0: item/myicon
```

```json
{
  "item_resource": {
    "parent": "item/generated",
    "textures": {
      "default": {
        "layer0": "item/myicon"
      }
    }
  }
}
```
<!-- langtabs-end -->

~~~

---

## `blockModelType` (required in most cases):

The block model to use/retexture. See [Block Model Types](block-model-types.md) for a list of options.

~~~admonish warning
For some block behaviours like `slab` it may be required to leave this field empty!
~~~

---

## `virtual`:

Flag whether the block should use an empty block model and virtual item display entity for the block. Block break particles will also be simulated on a packet-level, as the client-side block-model will be empty.

This allows for an infinite amount of blocks, but keep in mind that display entities are much heavier on the client than simple blocks! Large amounts of those virtual blocks might lag out clients!

This option might not work well with full block model types like `full_block`.

Defaults to `false`

---

## `properties`: 

The properties of this block. See [Block Properties](block-properties.md) for details.

---

## `group`

Defines the item-group for this blocks' item. See [Item Groups](../general/item-groups.md) for more information.

---

## `components`:

An object with minecraft components used for the item.

~~~admonish example
<!-- langtabs-start -->
```yml
components:
  minecraft:tool:
    default_mining_speed: 1.0
    damage_per_block: 2
    rules:
      - speed: 15
        correct_for_drops: true
        blocks: "#sword_efficient"
      - speed: 1.5
        blocks: cobweb
```

```json
{
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
  }
}
```
<!-- langtabs-end -->

~~~
