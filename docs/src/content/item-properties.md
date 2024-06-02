# Properties

Item properties are shared between all content types: Items, blocks and decorations.
All properties are optional.

Example with all properties set:
```json
{
  "id": "mynamespace:clown_horn",
  "vanillaItem": "minecraft:paper",
  "models": {
    "default": "mynamespace:custom/misc/clown_horn"
  },
  
  "properties": {
    "durability": 100,
    "stackSize": 64,
    "lore": ["line 1", "line 2"],
    "fireResistant": false
  }
}
```


### `durability`:

The items' `vanillaItem` will have to be a vanilla item that has a durability bar by default, otherwise it will not show up on the client side.

### `stackSize`:

Number of items a stack can hold.
Should be between 1 and 99
Values above 99 or 0 and below might cause problems™

### `lore`:

List of strings to use as item lore

### `fireResistant`:

Boolean (true/false) wether the item is fire resistant