# Decorations

The misode based generator for decorations can be found [here!](https://misode.tomalbrc.de/filament/decoration)

Decoration configuration files are to be placed in `MyDatapack/data/<namespace>/filament/decoration/my_decoration.json`.

All item-behaviours such as `fuel` and `cosmetic` are supported by decorations, as well as most block behaviours. You can also set components using the `components` field.

---

## Contents

<!-- langtabs-start -->
```yml
id: mynamespace:quartz_pedestal
translations:
  en_us: "Quartz Pedestal"
itemResource:
  models:
    default: mynamespace:custom/furniture/displays/quartz_pedastal
behaviour:
  showcase:
    - offset: [0, 1.05, 0]
      scale: [0.45, 0.45, 0.45]
      type: dynamic
  rotating:
    smooth: true # set to true to allow 45° rotations, false for 90° rotations 
blocks:
  - origin: [0, 0, 0]
    size: [1, 1, 1]
```

```json
{
  "id": "mynamespace:quartz_pedestal",
  "translations": {
    "en_us": "Quartz Pedestal"
  },
  "itemResource": {
    "models": {
      "default": "mynamespace:custom/furniture/displays/quartz_pedastal"
    }
  },
  "behaviour": {
    "showcase": [
      {
        "offset": [0, 1.05, 0],
        "scale": [0.45, 0.45, 0.45],
        "type": "dynamic"
      }
    ],
    "rotating": {
      "smooth": true
    }
  },
  "blocks": [
    {
      "origin": [0,0,0],
      "size": [1,1,1]
    }
  ]
}
```
<!-- langtabs-end -->

The file contents are very similar to that of blocks, except for additional behaviours exclusive to decorations. Decorations support most of the block behaviours.

---

## Placement models

You can specify different models for decorations when placed on walls, on the ceiling (underside of blocks), or on the floor.

<!-- langtabs-start -->
```yml
id: mynamespace:small_gold_coin_piles
itemFrame: true
itemResource:
  models:
    default: minecraft:item/diamond_sword
    wall: minecraft:custom/hats/backpack
    ceiling: minecraft:custom/furniture/misc/small_gold_coin_piles
    floor: minecraft:custom/furniture/misc/small_gold_coin_piles
block: minecraft:barrier
properties:
  placement:
    wall: true
    floor: true
    ceiling: true
behaviour:
  rotating:
    smooth: true
```

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
    "rotating": {
      "smooth": true
    }
  }
}
```
<!-- langtabs-end -->

This will result in a decoration with different models in the Inventory, when placed on the floor, when placed on a wall, and when placed on ceilings.

---

## Fields

### `itemFrame`

Forces the decoration to use a single item-frame entity instead of an item-display entity in combination with an interaction entity.

> [!NOTE]
> This will be ignored if the `size` field has been set!

**Example**

```json
{
  "itemFrame": true
}
```

### `block`

Specifies a different block to use instead of barrier blocks.
A block identifier (e.g. `minecraft:barrier`).

> [!NOTE]
> Only used when the decoration has blocks specified using the `blocks` field.

**Example**

```json
{
  "block": "minecraft:barrier"
}
```

### `size`

Specifies the size of the interaction entity.
A `[width, height]` list representing the interaction entity size.

> [!NOTE]
> This will be ignored if the `blocks` field has been set!

**Example**

```json
{
  "size": [1.0, 1.0]
}
```

### `block_resource`

Decorations are able to show a different item model based on the block state, similar to blocks, using a `block_resource`.

> [!NOTE]
> Different models for wall/floor/ceiling placements won't work when using this field!
