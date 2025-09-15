# Properties

Decoration properties share the same properties as [items](item-properties.md) and a few of [blocks](block-properties.md), like `solid` and `pushReaction`

~~~admonish example "Example of properties"
<!-- langtabs-start -->
```yml
properties:
  placement:
    floor: true
    wall: false
    ceiling: false
  glow: false
  solid: false
  display: fixed
  blockBase: minecraft:stone
  useItemParticles: true
  showBreakParticles: true
```

```json
{
  "properties": {
    "placement": {
      "floor": true, 
      "wall": false, 
      "ceiling": false
    },
    "glow": false,
    "solid": false,
    "display": "fixed",
    "blockBase": "minecraft:stone",
    "useItemParticles": true,
    "showBreakParticles": true
  }
}
```
<!-- langtabs-end -->

~~~

---

### `allowAdventureMode`:

Allows adventure mode players to interact with the decoration. Defaults to `false`

---

### `rotate`:

~~~admonish warning
This is deprecated!
Use the `horizontal_facing` behaviour to make a decoration rotate in the 4 cardinal directions
~~~

Boolean (true/false) indicating whether the decoration can rotate (90° intervals)

Defaults to `false`

---

### `rotateSmooth`:

~~~admonish warning
This is deprecated!
Use the `rotating` block behaviour!
~~~

Boolean (true/false) indicating whether the decoration can rotate in 45° intervals

Defaults to `false`

---

### `placement`:

A set of options for placement options. Possible keys: "floor", "wall", "ceiling".

You can specify different models for the different placements in the itemResource field.
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

~~~

Default values:
<!-- langtabs-start -->
```yml
placement:
  floor: true
  wall: false
  ceiling: false
```

```json
{
  "placement": {
    "floor": true,
    "wall": false,
    "ceiling": false
  }
}
```
<!-- langtabs-end -->

---

### `glow`:

Boolean (true/false) indicating whether the decoration ignores ambient light and assumes light level 15.

Defaults to `false`

---

### `waterloggable`:

~~~admonish warning
This is deprecated!
Use the `waterloggable` block behaviour!
~~~

Boolean (true/false) indicating whether the decoration blocks can be waterlogged.

Defaults to `true`

---

### `solid`:

Boolean (true/false) indicating whether the decoration is solid - can be flushed away by water if set to false.

Defaults to `true`

---

### `display`:

Changes the item_display value used for the Item Display Entity of the decoration. 

Can be `none`, `thirdperson_lefthand`, `thirdperson_righthand`, `firstperson_lefthand`, `firstperson_righthand`, `head`, `gui`, `ground`, and `fixed`. 

Defaults to `fixed`.

---

### `scale`:

Scale for non-animated decoration. Defaults to `[1, 1, 1]`

---

### `showBreakParticles`

Flag whether to show break particles. 

Defaults to `true`

---

### `blockBase`

Used for place & break sounds and particles if `useItemParticles` is set to `false`

Defaults to `minecraft:stone`

---

### `useItemParticles`

Flag whether to use item particles. Uses the particles of the block defined in `blockBase` if set to `false`. 

Defaults to `true`

---

### `showBreakParticles`

Flag whether to show break particles at all.

Defaults to `true`

---

### `itemFrame`

Uses an item frame for decorations instead of either blocks or an interaction entity with an item display entity. 

Defaults to `false`
