# Decoration Behaviours

Example of some behaviours for decorations:

~~~admonish example
```json
{
  "id": "mynamespace:clown_horn",
  "vanillaItem": "minecraft:paper",
  "itemResource": {
    "models": {
      "default": "mynamespace:custom/misc/clown_horn"
    }
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
~~~

# Behaviours

---

## `animation` behaviour

Defines an animation behaviour for decorations. Supports ajmodel/bbmodels.

Models are placed in `data/mynamespace/filament/model/mymodel.bbmodel`.

You would reference it as `mynamespace:mymodel` in the `model` field.

~~~admonish info "Configurable Fields"
- `model`: The name of the animated model associated with this animation (if applicable).
- `autoplay`: The name of the animation to autoplay (if specified).
~~~

~~~admonish example
```json
{
  "animation": {
    "model": "mynamespace:mymodel",
    "autoplay": "myAnimationName"
  }
}
```
~~~

---

## `container` behaviour

Defines a container behaviour for decorations.

Dropper/Hopper support is not implemented yet as of filament 0.10.7

Allows to create chests, trashcans, etc.

Works with the `animation` behaviour to play an animation defined in the bbmodel/ajmodel.

~~~admonish info "Configurable Fields"
- `name`: The name displayed in the container UI.
- `size`: The size of the container, has to be 5 slots or a multiple of 9, up to 6 rows of 9 slots.
- `purge`: Indicates whether the container's contents should be cleared when no player is viewing the inventory.
- `openAnimation`: The name of the animation to play when the container is opened (if applicable).
- `closeAnimation`: The name of the animation to play when the container is closed (if applicable).
~~~

~~~admonish example
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
~~~

---

## `lamp` behaviour

Allows you to create lamps that either switch on/off or cycle through a list of light levels on player interaction.

~~~admonish info "Configurable Fields"
- `on`: Light level to use for the 'on' state
- `off`: Light level to use for the 'off' state
- `cycle`: List of light levels to cycle through. 
~~~

~~~admonish example "Simple on / off lamp"
```json
{
  "lamp": {
    "on": 15,
    "off": 0
  }
}
```
~~~

~~~admonish example "Cycling lamp"
```json
{
  "lamp": {
    "cycle": [0, 2, 4, 6, 8, 10, 12, 14]
  }
}
```
~~~

---

## `interact_execute` / `lock` behaviour

Defines a behaviour that runs a command, for decorations, optionally with a key item.

This behaviour is available under 2 names, `interact_execute` and `lock`. The name `lock` exists to keep compatibility with older versions of filament / filament configs.

It's an analog to the `execute` item behaviour.

The command will only run once if a key is specified, the key can be empty to always run the commands/animations on interaction.

This uses Permission levels, So when using with custom commands remember to add a permission level or the command may not work.

~~~admonish info "Configurable Fields"
- `key`: The identifier of the key required to unlock. Optional, if left empty the unlockAnimation will play (if applicable) and the commands will be run and the decoration will be discarded based on the `discard` flag.
- `consumeKey`: Determines whether the key should be consumed upon unlocking.
- `discard`: Specifies whether the decoration should be destroyed after interacting with it.
- `unlockAnimation`: Name of the animation to play upon successful unlocking with a key (if applicable).
- `command`: Command to execute when the lock is successfully unlocked (if specified).
- `atBlock`: false/true flag whether the command should be run at the blocks' position
~~~

---

## `seat` behaviour

Defines a seating behaviour for decorations.

For chairs, benches, etc.

~~~admonish info "Configurable Fields"
- `offset`: The player seating offset.
- `direction`: The rotation offset of the seat in degrees. Defaults to `180`
~~~

~~~admonish example "Single seat"
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
~~~

---

## `showcase` behaviour

Defines a showcase behaviour for decorations.

Allows you to create shelves / item-frame like decorations.

~~~admonish info "Configurable Fields"
- `offset`: Offset for positioning the showcased item.
- `scale`: Scale of the showcased item.
- `rotation`: Rotation of the showcased item.
- `type`: Type to display, block for blocks (block display), item for items (item display), dynamic uses blocks if possible, otherwise item (block/item display).
- `filterItems`: Items to allow.
- `filterTags`: Items with given item tags to allow.
~~~

~~~admonish example "Single item showcase"
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
~~~

---

## `cosmetic` behaviour

Defines cosmetic behaviours for decorations, supporting animated Blockbench models for chestplates and simple item models.

Cosmetics are worn on the player using item display entities (except for the head slot)

~~~admonish info "Configurable Fields"
- `slot`: The equipment slot for the cosmetic (head or chest).
- `model`: Optional, the resource location of the animated blockbench or animated-java model for the cosmetic.
- `autoplay`: Optional, the name of the animation to autoplay, which should be loopable.
- `scale`: Scale of the chest cosmetic. Defaults to `[1 1 1]`
- `translation`: Translation of the chest cosmetic. Defaults to `[0 0 0]`.
~~~

~~~admonish example "Backpack"
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
~~~

---

## `flammable` behaviour

Makes the block flammable.

~~~admonish info "Configurable Fields"
- `burn`: Burn. Defaults to `5`
- `spread` Fire spread chance. Defaults to `20`
~~~
