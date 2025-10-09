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

Supports copper golem item transport based on block-tags: `c:chests/wooden` and `minecraft:copper_chests`

~~~admonish info "Configurable Fields"
- `name`: The default name displayed in the container UI.
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

Supports copper golem item transport based on block-tags: `c:chests/wooden` and `minecraft:copper_chests`

~~~admonish info "Configurable Fields"
- `name`: The default name displayed in the container UI.
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

The command will only run once if a key is specified, the key can be empty to always run the commands/animations on
interaction.
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
- `console`: Run as server/console instead of as player
~~~

---

## `interact_execute` behaviour

This behaviour runs a command, plays an animation and runs a command once the animation finished.

It behaves similar to the `lock` behaviour, but will always
It's similar to the `execute` item-behaviour or `interact_execute` decoration behaviour.

The command will only run once if a key is specified, the key can be empty to always run the commands/animations on
interaction.
The `repeatable` flag can be set to overwrite this.

The `command` or `commands` are run as player but with elevated permissions, optionally at the decoration block itself.

~~~admonish info "Configurable Fields"
- `key`: The identifier of the item held by player required to run commands/animations. Optional, if left empty the animation will play (if applicable) and the commands will be run and the decoration will be discarded based on the `discard` flag.
- `consumeKey`: Determines whether the key should be consumed upon unlocking.
- `animate_per_player`: Flag to play animations per-player. Defaults to `false`
- `discard`: Specifies whether the decoration should be destroyed after interacting with it.
- `animation`: Name of the animation to play when interacting.
- `animationPost`: Name of animation to player after the first one ended
- `command`: Command to execute when the lock is successfully unlocked (if a key is specified, otherwise the command is always run).
- `commands`: List of commands, as above
- `atBlock`: false/true flag whether the command should be run at the blocks' position
- `commandPostAnimation`: Command to run when the first animation stops playing
- `commandsPostAnimation`: List of commands, as above
- `commandIncorrectKey`: Command to run when the player has an incorrect key
- `commandsIncorrectKey`: List of commands, as above
- `animationIncorrectKey`: Animation to play when the player has an incorrect key
- `console`: Run as server/console instead of as player
~~~

---

## `break_execute` behaviour

This behaviour runs a command when the decoration is broken by a player

~~~admonish info "Configurable Fields"
- `command`: Command to execute when the lock is successfully unlocked (if specified).
- `commands`: List of commands to execute when interacted with
- `atBlock`: false/true flag whether the command should be run at the blocks' position
- `console`: Run as server/console instead of as player
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

Can be either a list of elements or an object as described below:

Supports copper golem item transport based on block-tags: `c:chests/wooden` and `minecraft:copper_chests`

~~~admonish info "Configurable Fields"
- `useMenu`: Use a container menu instead of the in-world insert interaction
- `name`: The default name displayed in the container UI.
- `canPickup`: Flag whether the container will not drop its items when broken but store it as component in the dropped item
- `showCustomName`: Show the name of the itemstack inside the container menu. Defaults to `true`
- `hopperDropperSupport`: Enables hopper and dropper interaction. Defaults to `true`
- `elements`: List of elements
~~~

~~~admonish info "Fields for each element"
- `offset`: Offset for positioning the showcased item.
- `scale`: Scale of the showcased item.
- `rotation`: Rotation of the showcased item.
- `type`: Display entity type to use, `block` for block display, `item` for item display, `dynamic` uses block displays if possible, otherwise item displays.
- `filterItems`: Items to allow.
- `filterTags`: Items with given item tags to allow.
- `addItemSound`: Sound to use when inserting an item by a player. Defaults to item frame sounds
- `removeItemSound`: Sound to use when an item is removed by a player. Defaults to item frame sounds
- `maxStackSize`: Max stack size for this showcase element. Defaults to 1
~~~

~~~admonish example "Showcase with menu"
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

~~~admonish example "Showcase with menu"
<!-- langtabs-start -->
```yml
showcase:
  hopperDropperSupport: true
  useMenu: true
  name: "Showcase"
  showCustomName: false
  canPickup: false
  elements:
    - offset: [0, 0, 0]
      scale: [1, 1, 1]
      rotation: [0, 0, 0]
      type: item
      addItemSound: minecraft:item_frame.add_item
      removeItemSound: minecraft:item_frame.remove_item
      maxStackSize: 1
```

```json
{
   "showcase":{
      "hopperDropperSupport": true,
      "useMenu": false,
      "name": "Showcase",
      "showCustomName":false,
      "canPickup":false,
      "elements":[
         {
            "offset":[0, 0, 0],
            "scale":[1, 1, 1],
            "rotation":[0, 0, 0],
            "type": "item",
            "addItemSound": "minecraft:item_frame.add_item",
            "removeItemSound": "minecraft:item_frame.remove_item",
            "maxStackSize": 1
         }
      ]
   }
}
```
<!-- langtabs-end -->

~~~

---

## `sign` behaviour

Allows you to create signs with multiple text displays.

Can be either a list of elements or an object as described below:

~~~admonish info "Configurable Fields"
- `canEdit`: Flag wether the sign elements can be edited. Defaults to `true`
- `waxable`: Flag wether the sign is waxable. Defaults to `true`
- `dyeable`: Flag wether the sign is dyeable. Defaults to `true`
- `block`: Sign block to use for the Sign Edit GUI. Defaults to `minecraft:oak_sign`
- `elements`: List of elements. See below
~~~

~~~admonish info "Fields for each element"
- `offset`: Offset for positioning the text element. Defaults to `[0, 0, 0.5]`
- `scale`: Scale of the text element. Defaults to `[0.5, 0.5, 0.5]`
- `rotation`: Rotation of the showcased item.
- `lines`: Number of lines for this sign element. Defaults to `4`
- `text`: List of predefined lines of text for this sign element.
- `billboardMode`: Billboard mode of this element. Can be `fixed`, `vertical`, `horizontal`, `center`. Defaults to `fixed`
- `backgroundColor`: Background color of the text element. Defaults to `0`
- `seeThrough`: See-through flag. Defaults to `false`
- `alignment`: Text alignment. Can be `center`, `left`, `right`. Defaults to `center`
~~~

~~~admonish example Sign
<!-- langtabs-start -->
```yml
sign:
  can_edit: true
  elements:
    - offset:
        - 0
        - 0
        - -0.51
      rotation:
        - 0
        - 180
        - 0
      text:
        - "<rainbow>Color test 2</rainbow>"
        - "Very cool!"
        - "this is the backside"
        - ""
    - offset:
        - 0
        - 0
        - -0.55
      text:
        - "<rainbow>Color test 1</rainbow>"
        - "Very cool!"
        - "this is the front side"
        - ""
```

```json
{
   "sign":{
      "can_edit":true,
      "elements":[
         {
            "offset":[
               0,
               0,
               -0.51
            ],
            "rotation":[
               0,
               180,
               0
            ],
            "text":[
               "<rainbow>Color test 2</rainbow>",
               "Very cool!",
               "this is the backside",
               ""
            ]
         },
         {
            "offset":[
               0,
               0,
               -0.55
            ],
            "text":[
               "<rainbow>Color test 1</rainbow>",
               "Very cool!",
               "this is the front side",
               ""
            ]
         }
      ]
   }
}
```
<!-- langtabs-end -->
~~~
