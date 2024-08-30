# Properties

All properties (excluding the shared item properties):
```json
{
  "properties": {
    "rotate": false,
    "rotateSmooth": false,
    "placement": {"floor": true, "wall": false, "ceiling": false},
    "glow": false,
    "waterloggable": true,
    "solid": false,
    "display": "fixed"
  }
}
```

### `rotate`:

Boolean (true/false) indicating whether the decoration can rotate (90° intervals)

### `rotateSmooth`:

Boolean (true/false) indicating whether the decoration can rotate in 45° intervals

### `placement`:

A set of options for placement options. Possible keys: "floor", "wall", "ceiling".

### `glow`:

Boolean (true/false) indicating whether the decoration ignores ambient light and assumes light level 15.

### `waterloggable`:

Boolean (true/false) indicating whether the decoration blocks can be waterlogged.

### `solid`:

Boolean (true/false) indicating whether the decoration is solid - can be flushed away by water if set to false.

### `display`:

Changes the item_display value used for the Item Display Entity of the decoration. 

Can be `none`, `thirdperson_lefthand`, `thirdperson_righthand`, `firstperson_lefthand`, `firstperson_righthand`, `head`, `gui`, `ground`, and `fixed`. Defaults to `fixed`.