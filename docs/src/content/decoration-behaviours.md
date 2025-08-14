# Decoration Behaviours

Example of some behaviours for decorations:

~~~admonish example
<!-- langtabs-start -->
```yml
id: mynamespace:clown_horn
vanillaItem: minecraft:paper
itemResource:
  models:
    default: mynamespace:custom/misc/clown_horn
behaviour:
  animation:
    model: mynamespace:mymodel
    autoplay: myAnimationName
  container:
    name: Example Container
    size: 9
    purge: false
    openAnimation: openAnimation
    closeAnimation: closeAnimation
  lock:
    key: minecraft:tripwire_hook
    consumeKey: false
    discard: false
    unlockAnimation: unlockAnimation
    command: "say Unlocked"
  seat:
    - offset: [0.0, 0.0, 0.0]
      direction: 0.0
  showcase:
    - offset: [0.0, 0.0, 0.0]
      scale: [1.0, 1.0, 1.0]
      rotation: [0.0, 0.0, 0.0, 1.0]
      type: item
      filterItems: null
      filterTags: null
  cosmetic:
    slot: head
    model: mynamespace:clown_backpack_animated
    autoplay: idle
    scale: [1.5, 1.5, 1.5]
    translation: [0.0, 0.5, 0.0]
```

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
<!-- langtabs-end -->

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
- `variant`: The model variant to display (for Animated-Java models). Can be mapped to a block-state 
~~~

~~~admonish example
<!-- langtabs-start -->
```yml
animation:
  model: mynamespace:mymodel
  autoplay: myAnimationName
```

```json
{
  "animation": {
    "model": "mynamespace:mymodel",
    "autoplay": "myAnimationName"
  }
}
```
<!-- langtabs-end -->

~~~

---

## `container` behaviour

Defines a container behaviour for decorations.

Allows to create barrels, trashcans, etc.

Works with the `animation` behaviour to play an animation defined in the bbmodel/ajblueprint.

~~~admonish info "Configurable Fields"
- `name`: The name displayed in the container UI.
- `showCustomName`: Flag whether to show the name of the placed itemstack. Enabled by default.
- `size`: The size of the container, has to be 5 slots or a multiple of 9, up to 6 rows of 9 slots.
- `purge`: Indicates whether the container's contents should be cleared when no player is viewing the inventory.
- `openAnimation`: The name of the animation to play when the container is opened (if applicable).
- `closeAnimation`: The name of the animation to play when the container is closed (if applicable).
- `canPickup`: Flag whether the container will not drop its items when broken but store it as component in the dropped item
- `hopperDropperSupport`: Flag whether hoppers/droppers can interact with this container
- `angerPiglins`: Flag whether opening the container will anger piglins. Enabled by default.
~~~

~~~admonish example
<!-- langtabs-start -->
```yml
container:
  name: Example Container
  size: 9
  purge: false
  openAnimation: openAnimation
  closeAnimation: closeAnimation
```

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
<!-- langtabs-end -->

~~~

---

## `animated_chest` behaviour

Allows to make animated connectable containers with a left and right side. 

Requires with the `animation` behaviour to play an animation defined in the bbmodel/ajblueprint.

~~~admonish info "Configurable Fields"
- `name`: The name displayed in the container UI.
- `showCustomName`: Flag whether to show the name of the placed itemstack if it has a custom name. Enabled by default.
- `size`: The size of the container, has to be 5 slots or a multiple of 9, up to 6 rows of 9 slots.
- `purge`: Indicates whether the container's contents should be cleared when no player is viewing the inventory.
- `openAnimation`: The name of the animation to play when the container is opened (if applicable).
- `closeAnimation`: The name of the animation to play when the container is closed (if applicable).
- `canPickup`: Flag whether the container will not drop its items when broken but store it as component in the dropped item
- `hopperDropperSupport`: Flag whether hoppers/droppers can interact with this container
- `angerPiglins`: Flag whether opening the container will anger piglins. Enabled by default.
- `ignoreBlock`: Flag whether the block in direction as specified in `blockDirection` prevents the chest from being opened. Enabled by default
- `blockDirection`: Direction for the `ignoreBlock` check. Can be `up`, `down`, `north`, `east`, `south`, `west`. `up` by default.
~~~

~~~admonish example
<!-- langtabs-start -->
```yml
animated_chest:
  name: Example Chest
  size: 27
  openAnimation: openAnimation
  closeAnimation: closeAnimation
```

```json
{
  "animated_chest": {
    "name": "Example Chest",
    "size": 27,
    "openAnimation": "openAnimation",
    "closeAnimation": "closeAnimation"
  }
}
```
<!-- langtabs-end -->

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
<!-- langtabs-start -->
```yml
seat:
  - offset: [0.0, 0.0, 0.0]
    direction: 0.0
```

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
<!-- langtabs-end -->

~~~

---

## `showcase` behaviour

Defines a showcase behaviour for decorations.

Allows you to create shelves / item-frame like decorations.

~~~admonish info "Configurable Fields"
- `offset`: Offset for positioning the showcased item.
- `scale`: Scale of the showcased item.
- `rotation`: Rotation of the showcased item.
- `type`: Display entity type to use, `block` for block display, `item` for item display, `dynamic` uses block displays if possible, otherwise item displays.
- `filterItems`: Items to allow.
- `filterTags`: Items with given item tags to allow.
- `addItemSound`: Sound to use when inserting an item by a player. Defaults to item frame sounds
- `removeItemSound`: Sound to use when an item is removed by a player. Defaults to item frame sounds
- `hopperDropperSupport`: Support for hoppers & droppers. If 1 showcase element has this option enabled, all will have it enabled. Enabled by default.
- `maxStackSize`: Max stack size for this showcase element. Defaults to 1
~~~

~~~admonish example "Single item showcase"
<!-- langtabs-start -->
```yml
showcase:
  - offset: [0.0, 0.0, 0.0]
    scale: [1.0, 1.0, 1.0]
    rotation: [0.0, 0.0, 0.0, 1.0]
    type: item
    filterItems:
      - minecraft:paper
    filterTags:
      - minecraft:tag_example
```

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
<!-- langtabs-end -->

~~~
