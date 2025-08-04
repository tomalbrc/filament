# Decorations

The misode based generator for decorations can be found [here!](https://misode.tomalbrc.de/filament/decoration)

## File

Decoration configuration files are to be placed in `MyDatapack/data/<namespace>/filament/decoration/my_decoration.json`.

All item-behaviours such as `fuel` and `cosmetic` are supported by decorations.

You can also set components to the configurations using the `components` field.

---

## Contents

~~~admonish example
<!-- langtabs-start -->
```yml
id: mynamespace:quartz_pedestal
translations:
  en_us: "Quartz Pedestal"
itemResource:
  models:
    default: mynamespace:custom/furniture/displays/quartz_pedastal
properties:
  rotate: true
  rotateSmooth: true
behaviour:
  showcase:
    - offset: [0, 1.05, 0]
      scale: [0.45, 0.45, 0.45]
      type: dynamic
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
  "properties": {
    "rotate": true,
    "rotateSmooth": true
  },
  "behaviour": {
    "showcase": [{
      "offset": [0, 1.05, 0],
      "scale": [0.45, 0.45, 0.45],
      "type": "dynamic"
    }]
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

~~~

The file contents are very similar to that of blocks, except for additional behaviours exclusive to decorations.

Decorations support most of the block behaviours.

You can specify different models for decorations when placed on walls, ceiling (underside of blocks) or on the floor.

~~~admonish example
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
    "rotating": { // to allow 90° rotations
      "smooth": true // to allow 45° rotations
    }
  }
}
```
<!-- langtabs-end -->

~~~

This will result in a decoration with different models in the Inventory, when placed on the floor, when placed on a wall and when placed on ceilings.

The field `block` can be used to specify a different block to use instead of barrier blocks (only used when the decoration has blocks specified using the `blocks` field)

The field `itemFrame` forces the decoration to use item-frames instead an item-display entity in combination with an interaction entity.