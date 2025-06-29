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
- `canPickup`: Flag whether the container will not drop its items when broken but store it as component in the dropped item
- `hopperDropperSupport`: Flag whether hoppers/droppers can interact with this container
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

## `lock` behaviour

This behaviour runs a command, optionally with a key item.

It's similar to the `execute` item-behaviour or `interact_execute` decoration behaviour.

The command will only run once if a key is specified, the key can be empty to always run the commands/animations on interaction.
The `repeatable` flag can be set to overwrite this.

The `command` or `commands` are run as player but with elevated permissions, optionally at the decoration block itself.

~~~admonish info "Configurable Fields"
- `key`: The identifier of the key required to unlock. Optional, if left empty the unlockAnimation will play (if applicable) and the commands will be run and the decoration will be discarded based on the `discard` flag.
- `consumeKey`: Determines whether the key should be consumed upon unlocking.
- `discard`: Specifies whether the decoration should be destroyed after interacting with it.
- `unlockAnimation`: Name of the animation to play upon successful unlocking with a key (if applicable).
- `command`: Command to execute when the lock is successfully unlocked (if specified).
- `commands`: List of commands to execute when the lock is successfully unlocked (if specified).
- `atBlock`: false/true flag whether the command should be run at the blocks' position
~~~

---

## `interact_execute` behaviour

This behaviour runs a command, plays an animation and runs a command once the animation finished.

It behaves similar to the `lock` behaviour, but will always 
It's similar to the `execute` item-behaviour or `interact_execute` decoration behaviour.

The command will only run once if a key is specified, the key can be empty to always run the commands/animations on interaction.
The `repeatable` flag can be set to overwrite this.

The `command` or `commands` are run as player but with elevated permissions, optionally at the decoration block itself.

~~~admonish info "Configurable Fields"
- `key`: The identifier of the item held by player required to run commands/animations. Optional, if left empty the animation will play (if applicable) and the commands will be run and the decoration will be discarded based on the `discard` flag.
- `consumeKey`: Determines whether the key should be consumed upon unlocking.
- `discard`: Specifies whether the decoration should be destroyed after interacting with it.
- `animation`: Name of the animation to play when interacting.
- `animationPost`: Name of animation to player after the first one ended
- `command`: Command to execute when the lock is successfully unlocked (if specified).
- `commands`: List of commands to execute when interacted with
- `atBlock`: false/true flag whether the command should be run at the blocks' position
- `commandPostAnimation`: Command to run when the first animation stops playing
- `commandsPostAnimation`: List of commands to run when the first animation stops playing
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
