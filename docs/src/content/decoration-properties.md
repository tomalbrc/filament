# Properties

Decoration properties share the same properties as [items](item-properties.md) and [blocks](block-properties.md)

All properties (excluding the shared item and block properties):
```json
{
  "properties": {
    "rotate": false,
    "rotateSmooth": false,
    "placement": {"floor": true, "wall": false, "ceiling": false},
    "glow": false,
    "waterloggable": true,
    "solid": false,
    "display": "fixed",
    "blockBase": "minecraft:stone",
    "useItemParticles": true,
    "showBreakParticles": true
  }
}
```

### `rotate`:

Boolean (true/false) indicating whether the decoration can rotate (90° intervals)

Defaults to `false`

### `rotateSmooth`:

Boolean (true/false) indicating whether the decoration can rotate in 45° intervals

Defaults to `false`

### `placement`:

A set of options for placement options. Possible keys: "floor", "wall", "ceiling".

Default values:
```json
{
  "placement": {
    "floor": true,
    "wall": false,
    "ceiling": false
  }
}
```

### `glow`:

Boolean (true/false) indicating whether the decoration ignores ambient light and assumes light level 15.

Defaults to `false`

### `waterloggable`:

Boolean (true/false) indicating whether the decoration blocks can be waterlogged.

Defaults to `true`

### `solid`:

Boolean (true/false) indicating whether the decoration is solid - can be flushed away by water if set to false.

Defaults to `true`

### `display`:

Changes the item_display value used for the Item Display Entity of the decoration. 

Can be `none`, `thirdperson_lefthand`, `thirdperson_righthand`, `firstperson_lefthand`, `firstperson_righthand`, `head`, `gui`, `ground`, and `fixed`. 

Defaults to `fixed`.

### `showBreakParticles`

Flag whether to show break particles. 

Defaults to `true`

### `blockBase`

Used for place & break sounds and particles if `useItemParticles` is set to `false`

Defaults to `minecraft:stone`

### `useItemParticles`

Flag whether to use item particles. Uses the particles of the block defined in `blockBase` if set to `false`. 

Defaults to `true`

### `showBreakParticles`

Flag whether to show break particles at all.

Defaults to `true`
