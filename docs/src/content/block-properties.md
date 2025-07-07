# Block Properties

Block properties share the same properties as [items](item-properties.md)

All properties (excluding the shared item and decoration properties):
~~~admonish example
<!-- langtabs-start -->
```yml
properties:
  blockBase: minecraft:stone
  requiresTool: true
  explosionResistance: 10
  destroyTime: 5
  redstoneConductor: false
  lightEmission: 0
  lootTable: minecraft:block/dirt
  sounds:
    volume: 1
    pitch: 0.2
    break: minecraft:entity.allay.hurt
    step: minecraft:entity.sniffer.hurt
    place: minecraft:entity.shulker.hurt
    hit: minecraft:entity.axolotl.hurt
    fall: minecraft:item.bone_meal.use
```

```json
{
  "properties": {
    "blockBase": "minecraft:stone",
    "requiresTool": true,
    "explosionResistance": 10,
    "destroyTime": 5,
    "redstoneConductor": false,
    "lightEmission": 0,
    "lootTable": "minecraft:block/dirt",
    "sounds": {
      "volume": 1,
      "pitch": 0.2,
      "break": "minecraft:entity.allay.hurt",
      "step": "minecraft:entity.sniffer.hurt",
      "place": "minecraft:entity.shulker.hurt",
      "hit": "minecraft:entity.axolotl.hurt",
      "fall": "minecraft:item.bone_meal.use"
    }
  }
}
```
<!-- langtabs-end -->

~~~

### `blockBase`:

Specifies the base block used for sounds and the block color on maps.

Defaults to `minecraft:stone`

### `requiresTool`:

Boolean (true/false) indicating whether the block requires a specific tool to be harvested.

Defaults to `true`

### `explosionResistance`:

Number indicating the block's resistance to explosions.

Defaults to `0`

### `destroyTime`:

Resistance of the block/the time required to destroy the block.

The destroyTime is used as explosionResistance if explosionResistance is not explicitly specified.

For indestructible blocks use a destroyTime of -1.

Defaults to `0`

### `redstoneConductor`:

Boolean (true/false) indicating whether the block can conduct redstone signals.

~~~admonish tip
All fields of this property can be mapped to a block-state.
~~~

The value of this property can be mapped to a blockstate like this:
~~~admonish example
<!-- langtabs-start -->
```yml
properties:
  redstoneConductor:
    powerlevel=0: true
    powerlevel=1: false
    # etc.
```

```json5
{
  "properties": {
    "redstoneConductor": {
      "powerlevel=0": true,
      "powerlevel=1": false,
      // etc.
    }
  }
}
```
<!-- langtabs-end -->

~~~

Defaults to `true`

### `lightEmission`:

Light level this block emits.

~~~admonish tip
All fields of this property can be mapped to a block-state.
~~~

The value of this property can be mapped to a blockstate like this:

~~~admonish example
<!-- langtabs-start -->
```yml
properties:
  lightEmission:
    powerlevel=0: 0
    powerlevel=1: 1
    # etc
```

```json5
{
  "properties": {
    "lightEmission": {
      "powerlevel=0": 0,
      "powerlevel=1": 1,
      // etc
    }
  }
}
```
<!-- langtabs-end -->

~~~

Defaults to `0`

### `transparent`:
Flag indicating whether the block is transparent. Transparent blocks don't block light.

Defaults to `false`

### `isSuffocating`
Flag whether the block causes suffocation damage.
The default is unset.

### `allowsSpawning`:
Flag indicating whether mobs can spawn on this block.

Defaults to `false`

### `replaceable`:
Flag indicating whether this block can be replaced by another block when placing a new block (e.g., grass can be replaced when placing a solid block).
Defaults to `false`

### `collision`:
Flag indicating whether the block has collision
Defaults to `true`

### `solid`:
Flag indicating whether the block gets flushed away with water.

Defaults to `true`

### `pushReaction`:
Specifies how the block reacts to being pushed by a piston. Possible values include normal, destroy, block

Defaults to `normal`

### `lootTable`:

Allows to override the default loot table

Example: `minecraft:blocks/dirt`

Defaults to `<namespace>:blocks/<blockname>`. 

So for a block "mynamespace:myblock", the default would be `mynamespace:blocks/myblock`

### `sounds`

Allows to specify a set of sounds and the volume/pitch for them.

All sounds, including step sounds, will be played serverside! Even for some vanilla blocks, since filament sets the vanilla sounds to an empty list, as a lot of blocks share the same sounds (noteblocks and all wooden blocks use the same wood sounds for example)

This might affect performance a bit, for this reason there is a config option in `config/filament.json` called "sound_module" to enable/disable the server-side block sounds.

~~~ admonish example
<!-- langtabs-start -->
```yml
properties:
  sounds:
    volume: 1
    pitch: 0.2
    break: minecraft:entity.allay.hurt   # sound when broken
    step: minecraft:entity.sniffer.hurt  # sound when stepped on
    place: minecraft:entity.shulker.hurt # sound when placed
    hit: minecraft:entity.axolotl.hurt   # sound when hit/being mined
    fall: minecraft:item.bone_meal.use   # sound when an entity falls on the block
```

```json
{
  "properties": {
    "sounds": {
      "volume": 1,
      "pitch": 0.2,
      "break": "minecraft:entity.allay.hurt", // sound when broken
      "step": "minecraft:entity.sniffer.hurt", // sound when stepped on
      "place": "minecraft:entity.shulker.hurt", // sound when placed
      "hit": "minecraft:entity.axolotl.hurt", // sound when hit/being mined
      "fall": "minecraft:item.bone_meal.use" // sound when an entity falls on the block
    }
  }
}
```
<!-- langtabs-end -->

~~~
