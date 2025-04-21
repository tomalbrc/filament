# Block Properties

Block properties share the same properties as [items](item-properties.md)

All properties (excluding the shared item and decoration properties):
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

### `blockBase`:

Specifies the base block for this block property. Must be a valid block identifier.
This is used for sounds, particles and the block color on maps.

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

Defaults to `true`

### `lightEmission`:

Light level this block emits.

~~~admonish tip
All fields of this property can be mapped to a block-state.
~~~

The value of this property can be mapped to a blockstate like this:

~~~admonish example
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
~~~

Defaults to `0`

### `transparent`:
Flag indicating whether the block is transparent. Transparent blocks don't block light.

Defaults to `false`

### `isSuffocating`
Flag wether the block causes suffocation damage.
The default is unset.

### `jumpFactor`
Allows to change the bounciness or stickiness of the block.
Set to a value below 1 to make the block act like honey, and above 1 to make it bouncy.

Defaults to `1.0`

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

Example: `minecraft:block/dirt`

Defaults to `<namespace>:block/<blockname>`. 

So for a block "mynamespace:myblock", the default would be `mynamespace:block/myblock`

### `sounds`

Allows to specify a set of sounds and the volume/pitch for them.

All sounds, including step sounds, will be played serverside! Even for some vanilla blocks, since filament sets the vanilla sounds to an empty list, as a lot of blocks share the same sounds (noteblocks and all wooden blocks use the same wood sounds for example)

This might affect performance a bit, for this reason there is a config option in `config/filament.json` called "sound_module" to enable/disable the server-side block sounds.

~~~ admonish example
```json
{
  "properties": {
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
~~~