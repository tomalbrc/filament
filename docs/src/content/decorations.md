# Decorations

## File

Decoration configuration files are to be placed in `MyDatapack/data/<namespace>/filament/decoration/my_decoration.json`.

All item-behaviours such as `fuel` and `cosmetic` are supported by decorations.

You can also set components to the configurations using the `components` field.

---

## Contents

~~~admonish example
```json5
{
  "id": "mynamespace:clown_horn",
  "vanillaItem": "minecraft:paper",
  "itemTags": ["minecraft:enchantable/trident"], // optional item tags
  "blockTags": ["minecraft:dirt"], // optional block tags
  "itemResource": {
    "models": {
      "default": "mynamespace:custom/misc/clown_horn"
    }
  },
  "group": "mynamespace:mygroup",
  "properties": {
    "stackSize": 1
  },
  "behaviour": {
    "instrument": {
      "sound": "mynamespace:misc.honk",
      "range": 64,
      "useDuration": 60
    }
  },
  "components": {
    // ...
  }
}
```
~~~

The file contents are very similar to that of blocks, except for additional behaviours exclusive to decorations.

Decorations support most of the block behaviours.

You can specify different models for decorations when placed on walls, ceiling (underside of blocks) or on the floor.

~~~admonish example
```json
{
  "id": "mynamespace:small_gold_coin_piles",
  "itemFrame": true,
  "itemResource": {
    "models": {
      "default": "minecraft:item/diamond_sword",
      "wall": "minecraft:custom/hats/backpack",
      "ceiling": "minecraft:custom/furniture/misc/small_gold_coin_piles",
      "floor": "minecraft:custom/furniture/misc/small_gold_coin_piles"
    }
  },
  "block": "minecraft:barrier",
  "properties": {
    "placement": {
      "wall": true,
      "floor": true,
      "ceiling": true
    }
  },
  "behaviour": {
    "rotating": { // to allow 90° rotations
      "smooth": true // to allow 45° rotations
    }
  }
}
```
~~~

This will result in a decoration with different models in the Inventory, when placed on the floor, when placed on a wall and when placed on ceilings.

The field `block` can be used to specify a different block to use instead of barrier blocks (only used when the decoration has blocks specified using the `blocks` field)

The field `itemFrame` forces the decoration to use item-frames instead an item-display entity in combination with an interaction entity.