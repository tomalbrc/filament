# Properties

All properties (excluding the shared item properties):
```json
"properties": {
  "rotate": false,
  "rotateSmooth": false,
  "placement": {"floor": true, "wall": false, "ceiling": false},
  "glow": false,
  "waterloggable": true,
  "solid": false
}
```

### `rotate`:

Boolean (true/false) indicating whether the decoration can rotate (90° intervals)

### `rotateSmooth`:

Boolean (true/false) indicating whether the decoration can rotate in 45° intervals

### `placement`:

An object with boolean properties indicating the placement options. Possible keys: "floor", "wall", "ceiling".

### `glow`:

Boolean (true/false) indicating whether the decoration ignore ambient light and assume light level 15.

### `waterloggable`:

Boolean (true/false) indicating whether the decoration blocks can be waterlogged.

### `solid`:

Boolean (true/false) indicating whether the decoration is solid - can be flushed away by water if set to false.
