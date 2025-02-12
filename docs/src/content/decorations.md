# Decorations

## File

Decoration configuration files are to be placed in `MyDatapack/data/<namespace>/filament/decoration/my_decoration.json`.

All item-behaviours such as `food`, `fuel` and `cosmetic` are supported by decorations.

You can also set components similar to item configurations using the `components` field

---

## Contents

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

The file contents are very similar to that of blocks, except for additional behaviours exclusive to decorations.

Decorations do not support most of the block behaviours.
