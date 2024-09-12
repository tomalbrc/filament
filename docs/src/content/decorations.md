# Decorations

## File

Decoration configuration files are to be placed in `MyDatapack/data/<namespace>/filament/decoration/my_decoration.json`.
Item behaviours such as `food`, `fuel` and `cosmetic` are supported by decorations.
You can also set components similar to item configurations using the `components` field

## Contents

```json
{
  "id": "mynamespace:clown_horn",
  "vanillaItem": "minecraft:paper",
  "models": {
    "default": "mynamespace:custom/misc/clown_horn"
  },
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
    ...
  }
}
```

The file contents are very similar to that of blocks, except for additional behaviours exclusive to decorations.

Decorations do not support most of the block behaviours.