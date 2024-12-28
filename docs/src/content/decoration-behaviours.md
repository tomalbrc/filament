# Decoration Behaviours

Example of some behaviours for decorations:

```json
{
  "id": "mynamespace:clown_horn",
  "vanillaItem": "minecraft:paper",
  "models": {
    "default": "mynamespace:custom/misc/clown_horn"
  },
  "behaviour": {
    "animation": {
      "model": "mynamespace:mymodel",
      "autoplay": "myAnimationName"
    },
    "container": {
      "name": "Example Container",
      "size": 9,
      "purge": false,
      "openAnimation": "openAnimation",
      "closeAnimation": "closeAnimation"
    },
    "lock": {
      "key": "minecraft:tripwire_hook",
      "consumeKey": false,
      "discard": false,
      "unlockAnimation": "unlockAnimation",
      "command": "say Unlocked"
    },
    "seat": [
      {
        "offset": [0.0, 0.0, 0.0],
        "direction": 0.0
      }
    ],
    "showcase": [
      {
        "offset": [0.0, 0.0, 0.0],
        "scale": [1.0, 1.0, 1.0],
        "rotation": [0.0, 0.0, 0.0, 1.0],
        "type": "item",
        "filterItems": null,
        "filterTags": null
      }
    ],
    "cosmetic": {
      "slot": "head",
      "model": "mynamespace:clown_backpack_animated",
      "autoplay": "idle",
      "scale": [1.5, 1.5, 1.5],
      "translation": [0.0, 0.5, 0.0]
    }
  }
}
```
---

## `animation` behaviour

Defines an animation behaviour for decorations. Supports ajmodel/bbmodels.

Models are placed in `data/mynamespace/filament/model/mymodel.bbmodel`.

You would reference it as `mynamespace:mymodel` in the `model` field.

### Fields:
- `model`: The name of the animated model associated with this animation (if applicable).
- `autoplay`: The name of the animation to autoplay (if specified).

### Example:
```json
{
  "animation": {
    "model": "mynamespace:mymodel",
    "autoplay": "myAnimationName"
  }
}
```

---

## `container` behaviour

Defines a container behaviour for decorations.

Dropper/Hopper support is not implemented yet as of filament 0.10.7

Allows to create chests, trashcans, etc.

Works with the `animation` behaviour to play an animation defined in the bbmodel/ajmodel.

### Fields:
- `name`: The name displayed in the container UI.
- `size`: The size of the container, has to be 5 slots or a multiple of 9, up to 6 rows of 9 slots.
- `purge`: Indicates whether the container's contents should be cleared when no player is viewing the inventory.
- `openAnimation`: The name of the animation to play when the container is opened (if applicable).
- `closeAnimation`: The name of the animation to play when the container is closed (if applicable).

### Example:
```json
{
  "container": {
    "name": "Example Container",
    "size": 9,
    "purge": false,
    "openAnimation": "openAnimation",
    "closeAnimation": "closeAnimation"
  }
}
```

---

## `interact_execute` behaviour

Allows you to run a command lamps that either switch on/off or cycle through a list of light levels on player interaction.

### Fields:
- `on`: Light level to use for the 'on' state
- `off`: Light level to use for the 'off' state
- `cycle`: List of light levels to cycle through.

### On/off lamp example:
```json
{
  "lock": {
    "on": 15,
    "off": 0
  }
}
```
### Cycling lamp example:
```json
{
  "lock": {
    "cycle": [0, 2, 4, 6, 8, 10, 12, 14]
  }
}
```

---

## `lamp` behaviour

Allows you to create lamps that either switch on/off or cycle through a list of light levels on player interaction.

### Fields:
- `on`: Light level to use for the 'on' state
- `off`: Light level to use for the 'off' state
- `cycle`: List of light levels to cycle through. 

### On/off lamp example:
```json
{
  "lock": {
    "on": 15,
    "off": 0
  }
}
```
### Cycling lamp example:
```json
{
  "lock": {
    "cycle": [0, 2, 4, 6, 8, 10, 12, 14]
  }
}
```

---

## `interact_execute` / `lock` behaviour

This behaviour is available under 2 names, `interact_execute` and `lock`. The name `lock` exists to keep compatibility with older versions of filament / filament configs.  

Defines a behaviour that runs a command, for decorations.

It's an analog to the `execute` item behaviour.

### Fields:
- `key`: The identifier of the key required to unlock.
- `consumeKey`: Determines whether the key should be consumed upon unlocking.
- `discard`: Specifies whether the lock utility should be discarded after unlocking.
- `unlockAnimation`: Name of the animation to play upon successful unlocking (if applicable).
- `command`: Command to execute when the lock is successfully unlocked (if specified).

---

## `seat` behaviour

Defines a seating behaviour for decorations.

For chairs, benches, etc.

### Fields:
- `offset`: The player seating offset.
- `direction`: The rotation offset of the seat in degrees. Defaults to `180`

### Single seat example:
```json
{
  "seat": [
    {
      "offset": [0.0, 0.0, 0.0],
      "direction": 0.0
    }
  ]
}
```

---

## `showcase` behaviour

Defines a showcase behaviour for decorations.

Allows you to create shelves / item-frame like decorations.

### Fields:
- `offset`: Offset for positioning the showcased item.
- `scale`: Scale of the showcased item.
- `rotation`: Rotation of the showcased item.
- `type`: Type to display, block for blocks (block display), item for items (item display), dynamic uses blocks if possible, otherwise item (block/item display).
- `filterItems`: Items to allow.
- `filterTags`: Items with given item tags to allow.

Single item showcase example:
```json
{
  "showcase": [
    {
      "offset": [0.0, 0.0, 0.0],
      "scale": [1.0, 1.0, 1.0],
      "rotation": [0.0, 0.0, 0.0, 1.0],
      "type": "item",
      "filterItems": ["minecraft:paper"],
      "filterTags": ["minecraft:tag_example"]
    }
  ]
}
```

---

## `cosmetic` behaviour

Defines cosmetic behaviours for decorations, supporting animated Blockbench models for chestplates and simple item models.

Cosmetics are worn on the player using item display entities (except for the head slot)

### Fields:
- `slot`: The equipment slot for the cosmetic (head or chest).
- `model`: Optional, the resource location of the animated blockbench or animated-java model for the cosmetic.
- `autoplay`: Optional, the name of the animation to autoplay, which should be loopable.
- `scale`: Scale of the chest cosmetic. Defaults to `[1 1 1]`
- `translation`: Translation of the chest cosmetic. Defaults to `[0 0 0]`.

### Backpack example:
```json
{
  "cosmetic": {
    "slot": "chest",
    "model": "mynamespace:clown_backpack_animated",
    "autoplay": "idle",
    "scale": [1.5, 1.5, 1.5],
    "translation": [0.0, 0.5, 0.0]
  }
}
```
