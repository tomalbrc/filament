# Block Behaviours

Block behaviours can be customized to add unique properties to blocks. All behaviours are optional and can be applied to both, blocks and decorations.

Blocks also work with most item-behaviours to give the blocks' item special features.

Some block behaviours provide blockstate properties you will have to provide models for, like `axis`, `repeater`, `crop`, `directional`, etc.

Example of a blockResource entry for the `repeater` behaviour (can be found as relay block in the example datapack):

~~~admonish example collapsible=true
<!-- langtabs-start -->
```yml
blockResource:
  models:
    facing=up,powered=false: minecraft:custom/block/arcanery/relay/relay_up_off
    facing=down,powered=false: minecraft:custom/block/arcanery/relay/relay_down_off
    facing=north,powered=false: minecraft:custom/block/arcanery/relay/relay_north_off
    facing=south,powered=false: minecraft:custom/block/arcanery/relay/relay_south_off
    facing=east,powered=false: minecraft:custom/block/arcanery/relay/relay_east_off
    facing=west,powered=false: minecraft:custom/block/arcanery/relay/relay_west_off
    facing=up,powered=true: minecraft:custom/block/arcanery/relay/relay_up_on
    facing=down,powered=true: minecraft:custom/block/arcanery/relay/relay_down_on
    facing=north,powered=true: minecraft:custom/block/arcanery/relay/relay_north_on
    facing=south,powered=true: minecraft:custom/block/arcanery/relay/relay_south_on
    facing=east,powered=true: minecraft:custom/block/arcanery/relay/relay_east_on
    facing=west,powered=true: minecraft:custom/block/arcanery/relay/relay_west_on
```

```json
{
  "blockResource": {
    "models": {
      "facing=up,powered=false": "minecraft:custom/block/arcanery/relay/relay_up_off",
      "facing=down,powered=false": "minecraft:custom/block/arcanery/relay/relay_down_off",
      "facing=north,powered=false": "minecraft:custom/block/arcanery/relay/relay_north_off",
      "facing=south,powered=false": "minecraft:custom/block/arcanery/relay/relay_south_off",
      "facing=east,powered=false": "minecraft:custom/block/arcanery/relay/relay_east_off",
      "facing=west,powered=false": "minecraft:custom/block/arcanery/relay/relay_west_off",
      "facing=up,powered=true": "minecraft:custom/block/arcanery/relay/relay_up_on",
      "facing=down,powered=true": "minecraft:custom/block/arcanery/relay/relay_down_on",
      "facing=north,powered=true": "minecraft:custom/block/arcanery/relay/relay_north_on",
      "facing=south,powered=true": "minecraft:custom/block/arcanery/relay/relay_south_on",
      "facing=east,powered=true": "minecraft:custom/block/arcanery/relay/relay_east_on",
      "facing=west,powered=true": "minecraft:custom/block/arcanery/relay/relay_west_on"
    }
  }
}
```
<!-- langtabs-end -->

You can also provide rotations for the block model like so:
<!-- langtabs-start -->
```yml
blockResource:
  models:
    lit=false,facing=north:
      model: minecraft:block/furnace
      y: 0
    lit=false,facing=south:
      model: minecraft:block/furnace
      y: 180
    lit=false,facing=east:
      model: minecraft:block/furnace
      y: 90
    lit=false,facing=west:
      model: minecraft:block/furnace
      y: 270
    lit=true,facing=north:
      model: minecraft:block/furnace_on
      y: 0
    lit=true,facing=south:
      model: minecraft:block/furnace_on
      y: 180
    lit=true,facing=east:
      model: minecraft:block/furnace_on
      y: 90
    lit=true,facing=west:
      model: minecraft:block/furnace_on
      y: 270
```

```json
{
  "blockResource": {
    "models": {
      "lit=false,facing=north": { "model": "minecraft:block/furnace", "y": 0 },
      "lit=false,facing=south": { "model": "minecraft:block/furnace", "y": 180 },
      "lit=false,facing=east": { "model": "minecraft:block/furnace", "y": 90 },
      "lit=false,facing=west": { "model": "minecraft:block/furnace", "y": 270 },
      "lit=true,facing=north": { "model": "minecraft:block/furnace_on", "y": 0 },
      "lit=true,facing=south": { "model": "minecraft:block/furnace_on", "y": 180 },
      "lit=true,facing=east": { "model": "minecraft:block/furnace_on", "y": 90 },
      "lit=true,facing=west": { "model": "minecraft:block/furnace_on", "y": 270 }
    }
  }
}
```
<!-- langtabs-end -->

~~~

~~~admonish example title="Example of a block with behaviours set", collapsible=true
<!-- langtabs-start -->
```yml
id: mynamespace:myblock
blockResource:
  models:
    lit=false,facing=north:
      model: minecraft:block/furnace
      y: 0
    lit=false,facing=south:
      model: minecraft:block/furnace
      y: 180
    lit=false,facing=east:
      model: minecraft:block/furnace
      y: 90
    lit=false,facing=west:
      model: minecraft:block/furnace
      y: 270
    lit=true,facing=north:
      model: minecraft:block/furnace_on
      y: 0
    lit=true,facing=south:
      model: minecraft:block/furnace_on
      y: 180
    lit=true,facing=east:
      model: minecraft:block/furnace_on
      y: 90
    lit=true,facing=west:
      model: minecraft:block/furnace_on
      y: 270
blockModelType: full_block
properties:
  destroyTime: 0
  blockBase: minecraft:stone
behaviour:
  powersource:
    value: 15
  repeater:
    delay: 1
    loss: 1
  fuel:
    value: 10
  cosmetic:
    slot: chest
    model: mynamespace:custom/models/clown_backpack_animated
    autoplay: idle
    scale: [1.5, 1.5, 1.5]
    translation: [0.0, 0.5, 0.0]
  strippable:
    replacement: minecraft:stone
```

```json
{
  "id": "mynamespace:myblock",
    "blockResource": {
    "models": {
      "lit=false,facing=north": { "model": "minecraft:block/furnace", "y": 0 },
      "lit=false,facing=south": { "model": "minecraft:block/furnace", "y": 180 },
      "lit=false,facing=east": { "model": "minecraft:block/furnace", "y": 90 },
      "lit=false,facing=west": { "model": "minecraft:block/furnace", "y": 270 },
      "lit=true,facing=north": { "model": "minecraft:block/furnace_on", "y": 0 },
      "lit=true,facing=south": { "model": "minecraft:block/furnace_on", "y": 180 },
      "lit=true,facing=east": { "model": "minecraft:block/furnace_on", "y": 90 },
      "lit=true,facing=west": { "model": "minecraft:block/furnace_on", "y": 270 }
    }
  },
  "blockModelType": "full_block",
  "properties": {
    "destroyTime": 0,
    "blockBase": "minecraft:stone"
  },
  "behaviour": {
    "powersource": {
      "value": 15
    },
    "repeater": {
      "delay": 1,
      "loss": 1
    },
    "fuel": {
      "value": 10
    },
    "cosmetic": {
      "slot": "chest",
      "model": "mynamespace:custom/models/clown_backpack_animated",
      "autoplay": "idle",
      "scale": [1.5, 1.5, 1.5],
      "translation": [0.0, 0.5, 0.0]
    },
    "strippable": {
      "replacement": "minecraft:stone"
    }
  }
}
```
<!-- langtabs-end -->

~~~

This creates a block + item that can be worn and when worn shows an animated blockbench model on the player. The item is also a food and can be used as fuel source in furnaces.

The block acts as a redstone powersource of level 15 and a repeater/relay and is strippable (turns to stone when stripped with an axe or an item with the `stripper` item behaviour)

While possible, you probably don't want to combine `powersource` with `repeater` for actual blocks/items for obvious reasons.

---

# Behaviours

## `axis` behaviour

Gives the block an `axis` property/block-state similar to wooden logs/pillars and handles placement.

~~~admonish info "Block-State-Properties to provide models for"
- `axis`: x, y, z
~~~

---

## `connectable` behaviour

This behaviour allows the block to connect to other blocks, similar to stairs, but requiring a block on both sides of a corner to be present in order to the corner shape.
Optionally without the corner states.

~~~admonish info "Block-State-Properties to provide models for"
- `shape`: `middle`, `single`, `left`, `right`, `inner_left`, `inner_right`, `outer_left`, `outer_right`
- `facing`: `north`, `east`, `south`, `west`
~~~

~~~admonish info "Configurable Fields"
- `corners`: Flag whether to allow corners
~~~

This behaviour is best used with decorations, as you will only have to define 6 models:
~~~admonish example
<!-- langtabs-start -->
```yml
itemResource:
  models:
    default: minecraft:custom/furniture/benches/middle_connectable
    inner: minecraft:custom/furniture/benches/inner_connectable
    outer: minecraft:custom/furniture/benches/outer_connectable
    middle: minecraft:custom/furniture/benches/middle_connectable
    left: minecraft:custom/furniture/benches/left_connectable
    right: minecraft:custom/furniture/benches/right_connectable
    single: minecraft:custom/furniture/benches/single_connectable
```

```json
{
  "itemResource": {
    "models": {
      "default": "minecraft:custom/furniture/benches/middle_connectable",
      "inner": "minecraft:custom/furniture/benches/inner_connectable",
      "outer": "minecraft:custom/furniture/benches/outer_connectable",
      "middle": "minecraft:custom/furniture/benches/middle_connectable",
      "left": "minecraft:custom/furniture/benches/left_connectable",
      "right": "minecraft:custom/furniture/benches/right_connectable",
      "single": "minecraft:custom/furniture/benches/single_connectable"
    }
  }
}
```
<!-- langtabs-end -->

~~~

---

## `count` behaviour

Gives the block a `count` property/block-state.

Works similar to turtle eggs or candles, allows you to place "multiple blocks/items" into one block.

~~~admonish info "Block-State-Properties to provide models for"
- `count`: 1...max
~~~

~~~admonish info "Configurable Fields"
- `max`: Max count
~~~

---

## `facing` behaviour

Gives the block a `facing` property/block-state similar to wooden logs/pillars and handles placement.

~~~admonish info "Block-State-Properties to provide models for"
  - `facing`: north, east, south, west, up, down
~~~

---

## `horizontal_facing` behaviour

Gives the block a `facing` property/block-state similar to furnaces and handles placement.
Does not support up and down facing directions.

~~~admonish info "Block-State-Properties to provide models for"
- `facing`: north, east, south, west
~~~

---

## `crop` behaviour

Makes the block behave like a crop, bonemeable, growing, minimum light requirement, etc.
Also gives a growth bonus similar to vanilla crops, which check for farmland blocks in a 3x3 area centered below the crop block.
The bonus block and radius can be configured.

You probably want to use this behaviour together with the `can_survive` behaviour.

If you want your custom crop block to *not* turn farmland into dirt *without* moisture, like vanilla crops do, add your block to the block tag `maintains_farmland`.

In order for the farmland to not turn into dirt when placing the crop on top of it, make sure the `solid` property is set to `false`

For bee pollination, use the block tag `bee_growables`.

You can make farmer villagers able to plant the seeds using the item tag `villager_plantable_seeds`. Villagers will only work on crops that are on top of farmland blocks (vanilla limitation).

~~~admonish info "Block-State-Properties to provide models for"
- `age`: 0...maxAge-1
~~~

~~~admonish info "Configurable Fields"
- `maxAge`: maximum age steps of this block (from 0 to maxAge-1). Defaults to 4.
- `minLightLevel`: Minimum light level this crop needs to survive. Defaults to 8.
- `bonusRadius`: Radius to check for bonus blocks for. Defaults to 1.
- `bonusBlock`: Bonus block to check for. More bonus blocks means faster growth. Defaults to `minecraft:farmland`.
- `villagerInteraction`: Allows farmer villagers to break and plant the custom crop. Defaults to `true`.
- `beeInteraction`: Allows bees to pollinate the crop to increase its age. Defaults to `true`.
~~~

---

## `sapling` behaviour

Makes your block behave like vanilla saplings, growing based on random ticks and bonemealable.

All identifiers for the configured_placements are optional, they will only get used when configured.

You add your own configured placement for trees using vanilla datapack mechanics.

~~~admonish tip
#### Checkout the example datapack for the test_tree block!
~~~

~~~admonish info "Block-State-Properties to provide models for"
- `stage`: 0 to 1. You can provide a single model to use for both states, use `default` as key in that case.
~~~

~~~admonish info "Configurable Fields"
- `tree`: Identifier for a configured_placement (add via datapack or use vanilla ones)
- `minLightLevel`: Defaults to `9`
- `secondaryChance`: Chance between 0 and 1 for `secondaryMegaTree` or `secondaryFlowers` placement to be used. Defaults to `0`
- `randomTickGrowthChance`: Defaults to `0.15`
- `bonemealGrowthChance`: Defaults to `0.45`
- `megaTree`: Identifier for a configured_placement. Will get used for 2x2 sapling placements
- `secondaryMegaTree`: Identifier for a configured_placement. Alternative to `megaTree` based on `secondaryChance`
- `tree`: Identifier for a configured_placement. Normal tree without flower
- `secondaryTree`: Identifier for a configured_placement. Alternative to `tree` based on `secondaryChance`
- `flowers`: Identifier for a configured_placement. Used when there is a flower neaby.
- `secondaryFlowers`: Identifier for a configured_placement. Alternative to `flowers` based on `secondaryChance`
~~~

---

## `can_survive` behaviour

Checks for the block below with one of the configured block tags or blocks list.
The block will break off, similar to flowers or crops, when the block below them is not supported.

The behaviour will automatically check for and apply any `facing` or `axis` block-state properties.

Useful for bushes/plants/crops/flowers and more

~~~admonish info "Configurable Fields"
  - `blocks`: List of blocks this block can survive on. 
    - Example: `blocks: ["minecraft:stone", "minecraft:sand"]`
  - `tags`: List of block-tags this block can survive on. 
    - Example: `tags: ["minecraft:dirt", "minecraft:sculk_replaceable"]`
~~~

---

## `powersource` behaviour

Defines the block as a redstone power source.

~~~admonish info "Configurable Fields"
- `value`: The redstone power value the block emits (can be mapped). Defaults to 15
~~~

~~~admonish tip
The field of this behaviour can be mapped to a block-state.
~~~

~~~admonish example
<!-- langtabs-start -->
```yml
behaviour:
  powersource:
    value:
      age=0: 0
      age=1: 15
```

```json
{
  "behaviour": {
    "powersource": {
      "value": {
        "age=0": 0,
        "age=1": 15
      }
    }
  }
}
```
<!-- langtabs-end -->

~~~

~~~admonish example "Example with constant value"
<!-- langtabs-start -->
```yml
behaviour:
  powersource:
    value: 5
```

```json
{
  "behaviour": {
    "powersource": {
      "value": 5
    }
  }
}
```
<!-- langtabs-end -->

~~~

---

## `repeater` behaviour

Defines the block as a redstone repeater with configurable delay and loss.

~~~admonish info "Configurable Fields"
- `delay`: Delay in ticks. Defaults to 0
- `loss`: Power loss during transfer. Defaults to 0
~~~

~~~admonish info "Block-State-Properties to provide models for"
- `powered`: true, false
- `facing`: north, east, south, west, up, down
~~~

---

## `powerlevel` behaviour

Supplies a `powerlevel` blockstate and changes to it depending on the input redstone signal.

~~~admonish info "Configurable Fields"
- `max`: Maximum powerlevel this block can display
~~~

~~~admonish info "Block-State-Properties to provide models for"
- `powerlevel`: 0...max-1
~~~

---

## `strippable` behaviour

Defines the block as strippable, replacing it with another block when interacted with an axe.

~~~admonish info "Configurable Fields"
- `replacement`: The identifier of the block to replace the current block with. Example: `minecraft:stone`
- `lootTable`: Identifier for a loot table to use when the block is stripped. Example: `minecraft:bell`
- `scrape`: Flag whether to show copper scrape particles. Defaults to `false`
- `scrapeWax`: Flag whether to show wax scrape particles. Defaults to `false`
- `sound`: Custom sound id to play. Defaults to `minecraft:item.axe.strip`
~~~

---

## `slab` behaviour

Defines the block as slab, top, bottom, double, with placements, waterloggable.

~~~admonish info "Block-State-Properties to provide models for"
- `type`: top, bottom, double
- `waterlogged`: true, false
~~~

---

## `trapdoor` behaviour

Trapdoor like block.

~~~admonish info "Block-State-Properties to provide models for"
- `facing`: north, south, east, west, up, down
- `half`: top, bottom
- `open`: true, false
- `waterlogged`: true, false
~~~

~~~admonish info "Configurable Fields"
- `canOpenByWindCharge`: Whether the trapdoor can be opened by a wind charge. Defaults to `true`
- `canOpenByHand`: Whether the trapdoor can be opened by hand. Defaults to `true`
- `openSound`: Open sound. Defaults to wooden trapdoor open sound.
- `closeSound` = Close sound. Defaults to wooden trapdoor close sound.
~~~

---

## `door` behaviour

Door-like "block" that is 2 blocks high.
Comes with all door block state properties (hinge, open, powered, etc.)

~~~admonish info "Block-State-Properties to provide models for"
- `facing`: north, south, east, west, up, down
- `half`: lower, upper
- `open`: true, false
- `hinge`: left, right
~~~

~~~admonish info "Configurable Fields"
- `canOpenByWindCharge`: Whether the door can be opened by a wind charge. Defaults to `true`
- `canOpenByHand`: Whether the door can be opened by hand. Defaults to `true`
- `openSound`: Open sound. Defaults to wooden door open sound.
- `closeSound` = Close sound. Defaults to wooden door close sound.
~~~

---

## `waterloggable` behaviour

Simple waterloggable block with a `waterlogged` property.

There is an alias called `simple_waterlogged` for backwards compatibility with older filament data packs


~~~admonish info "Optional Block-State-Properties to provide models for"
- `waterlogged`: true, false
~~~

---

## `drop_xp` behaviour

Makes the block drop xp when being mined without the silk-touch enchantment.

~~~admonish info "Configurable Fields"
- `min`: Minimum amount of XP to drop
- `max`: Maximum amount of XP to drop
~~~

~~~admonish tip
The values of the `min` and `max` fields can be mapped to block-states.
~~~

~~~admonish example
<!-- langtabs-start -->
```yml
behaviour:
  drop_xp:
    min:
      age=0: 0
      age=1: 0
      age=2: 4
    max:
      age=0: 0
      age=1: 0
      age=2: 6
```

```json
{
  "behaviour": {
    "drop_xp": {
      "min": {
        "age=0": 0,
        "age=1": 0,
        "age=2": 4
      },
      "max": {
        "age=0": 0,
        "age=1": 0,
        "age=2": 6
      }
    }
  }
}
```
<!-- langtabs-end -->

Using behaviour for crops, you could make a crop that drops xp when fully aged 
~~~

~~~admonish example "Example with constant values"
<!-- langtabs-start -->
```yml
behaviour:
  drop_xp: 6
```

```json
{
  "behaviour": {
    "drop_xp": 6
  }
}
```
<!-- langtabs-end -->

This will drop 6 xp for any block-state
~~~

---

## `block_interact_execute` behaviour

Executes a command on block interaction from console, as player (@s).

~~~admonish info "Configurable Fields"
- `consumes`: Flag whether the block should be broken after running the command(s). Defaults to `false`
- `dropBlock`: Flag whether the block should drop items when broken. Defaults to `false`
- `command`: The command string to execute. Empty by default
- `commands`: List of commands to execute. Empty by default
- `atBlock`: false/true flag whether the command should be run at the blocks' position
- `sound`: Optional sound effect to play during execution. Empty by default
~~~

---

## `block_attack_execute` behaviour

Executes a command when the block is "attacked", from console, as player (@s).

~~~admonish info "Configurable Fields"
- `consumes`: Flag whether the block should be broken after running the command(s). Defaults to `false`
- `dropBlock`: Flag whether the block should drop items when broken. Defaults to `false`
- `command`: The command string to execute. Empty by default
- `commands`: List of commands to execute. Empty by default
- `atBlock`: false/true flag whether the command should be run at the blocks' position
- `sound`: Optional sound effect to play during execution. Empty by default
~~~

---

## `oxidizable` behaviour

Defines the block as oxidizing block, similar to the vanilla copper blocks, randomly replacing it with another block when it "ages". Can be reverted/scraped by axes and resets with lightning bolts like vanilla copper blocks.

~~~admonish info "Configurable Fields"
  - `replacement`: The identifier of the block to replace the current block with (e.g., "minecraft:stone").
  - `weatherState`: The current weathering state of this block. Can be `unaffected`, `exposed`, `weathered`, `oxidized`. Defaults to `unaffected`. A `weatherState` of `oxidized` will not oxidize any further.
~~~

---

## `budding` behaviour

With this behaviour the blocks grows other blocks, similar to budding amethyst blocks.
The sides, blocks and chance can be configured.

If the blocks in `grows` have directional/facing block state properties, they direction of the side the block is growing from will be set.

~~~admonish info "Configurable Fields"
- `chance`: Chance of the block to grow another block or move a block to the next growth stage in percent, from 0 to 100. Defaults to 20
- `sides`: List of sides blocks can grow out. Can be `north`, `south`, `east`, `west`, `up` or `down`. Defaults to all directions
- `grows`: List of id's of blocks for the growth stages. Example: `["minecraft:chain", "minecraft:end_rod"]`
~~~

---

## `grass_spread` behaviour

Allows the block to spread like mycelium or grass blocks do, based on random ticks.

~~~admonish info "Configurable Fields"
- `decayBlockState`: Chance of the block to grow another block or move a block to the next growth stage in percent, from 0 to 100. Defaults to 20
- `propagatesToBlocks`: List of List of blocks this block can spread to. Defaults to `["minecraft:dirt"]`
- `propagatesToBlockTags`: List of block tags this block can spread to. Empty by default
~~~

---

## `falling_block` behaviour

Makes the block a gravity affected/falling block like sand or anvils.

~~~admonish tip
All fields of this behaviour can be mapped to a block-state.
~~~

~~~admonish info "Configurable Fields"
- `delayAfterPlace`: Delay in ticks before the block falls. Defaults to `2`
- `heavy`: To cause anvil-like damage. Defaults to `false`
- `damagePerDistance`: Accumulated damage per block fallen
- `maxDamage`: Maximum damage a falling block can deal
- `disableDrops`: Prevent the block from being placed when it falls. This behaves like the `CancelDrop` NBT data for falling block entities. Defaults to `false`
- `dropItem`: Flag whether the block should drop as item when it breaks. This behaves like the `DropItems` NBT data for falling block entities. Defaults to `true`
- `silent`: Flag whether sounds are played when the block falls or breaks
- `landSound`: Sound played when the block lands
- `breakSound`: Sound played when the block breaks
- `canBeDamaged`: Flag whether the block should be placed as the block in `damagedBlock`
- `damagedBlock`: New block to use when the falling block 'breaks'. Will copy applicable block state properties
- `baseBreakChance`: Chance for the block to break into the block in `damagedBlock` on its own
- `breakChancePerDistance`: Chance increase per block fallen for the block to break into the block in `damagedBlock`
~~~

~~~admonish example
<!-- langtabs-start -->
```yml
falling_block:
  delayAfterPlace: 2 # delay in ticks before the block falls
  heavy: true # to cause anvil-like damage
  damagePerDistance: 2.0 # accumulated damage per block fallen
  maxDamage: 40 # maximum damage
  disableDrops: false # prevent the block from being placed
  silent: false # no sounds
  landSound: minecraft:block.anvil.land
  breakSound: minecraft:block.anvil.destroy
  canBeDamaged: true # flag whether the block should be placed as the block in "damagedBlock"
  damagedBlock: minecraft:diamond_block # new block to use, will copy applicable block state property
  baseBreakChance: 0.05 # chance for the block to "break" to the block in "damagedBlock"
  breakChancePerDistance: 0.05 # chance increase per block fallen

```

```json5
{
  "falling_block": {
    "delayAfterPlace": 2, // delay in ticks before the block falls
    "heavy": true, // to cause anvil-like damage
    "damagePerDistance": 2.0, // accumulated damage per block fallen
    "maxDamage": 40, // maximum damage
    "disableDrops": false, // prevent the block from being placed
    "silent": false, // no sounds
    "landSound": "minecraft:block.anvil.land",
    "breakSound": "minecraft:block.anvil.destroy",
    "canBeDamaged": true, // flag whether the block should be placed as the block in "damagedBlock"
    "damagedBlock": "minecraft:diamond_block", // new block to use, will copy applicable block state property
    "baseBreakChance": 0.05, // chance for the block to "break" to the block in "damagedBlock"
    "breakChancePerDistance": 0.05 // chance increase per block fallen
  }
}
```
<!-- langtabs-end -->

~~~

---

## `tnt` behaviour

With this behaviour the block can be lit with flint and steel or redstone to spawn a TNT entity with the blockstate of this block.

~~~admonish tip
All fields of this behaviour can be mapped to a block-state.
~~~

~~~admonish info "Configurable Fields"
- unstable: Flag whether the block explodes when a player tries to break it. Defaults to `false`
- explosionPower: Explosion power. Defaults to `4.0`
- fuseTime: Fuse time (delay until the tnt entity explodes). Defaults to `80`
- primeSound: Sound to play when the block is primed. Defaults to `minecraft:entity.tnt.primed`
~~~

---

# Block behaviours with block entity
The following block-behaviours require a block entity. They can not be pushed, so make sure the pushReaction in your block properties is set to `destroy`, `block` or `ignore`.

---

## `furnace` behaviour

Furnace behaviour with menu.

~~~admonish tip
Combine this with the `horizontal_facing` behaviour for a vanilla-like furnace block!
Checkout the example datapack to see how to configure it.
~~~

~~~admonish info "Block-State-Properties to provide models for"
- `lit`: true, false
~~~

---

## `hopper` behaviour

Hopper behaviour with menu.

~~~admonish info "Block-State-Properties to provide models for"
- `facing`: `north`, `south`, `east`, `west`, `down`
- `enabled`: `true`, `false`
~~~

~~~admonish info "Configurable Fields"
- `filterItems`: List of items and/or item tags. Defaults is empty. Example: `["minecraft:diamond", "#minecraft:dirt"]` would allow for diamonds and all items with the `minecraft:dirt` item-tag.
- `pickupItemEntities`: Flag whether item entities should be picked up. Defaults to `true`
- `takeFromContainer`: Flag whether items from the container above should be taken. Defaults to `true`
- `cooldownTime` Cooldown time before transfering a new item. Defaults to `8`
~~~

---

## `flammable` behaviour

Makes the block flammable.

~~~admonish info "Configurable Fields"
- `burn`: Burn. Defaults to `5`
- `spread` Fire spread chance. Defaults to `20`
~~~

## `leaf_decay` behaviour

Makes the block decay like leaf blocks when no log is attached to it and wasn't placed by a player.
Adds `distance` and `persistent` block state properties to the block.

~~~admonish info "Configurable Fields"
- `blockTag`: Block tag for blocks that prevent leaf decay. Defaults to `minecraft:logs`
- `decayChance` Chance to decay in each random tick. Can be between 0 and 1. Defaults to `1`
~~~

---

## `lamp` behaviour

Allows you to create lamps that either switch on/off or cycle through a list of light levels on player interaction.

~~~admonish info "Block-State-Properties to provide models for (optional)"
- `level`: 0 to 15 (optional when `models` is set to `true`)
~~~

~~~admonish info "Configurable Fields"
- `on`: Light level to use for the 'on' state. From `0`-`15`
- `off`: Light level to use for the 'off' state. From `0`-`15`
- `cycle`: List of light levels to cycle through. Overwrites the `on` and `off` values. Example: `[0,7,15]`
- `defaultValue`: Default light level when placed. From `0`-`15`
- `models`: Flag whether to use block state models. Ignored by decorations. `true`/`false`. `false` by default.
~~~

~~~admonish example "Simple on / off lamp"
<!-- langtabs-start -->
```yml
lamp:
  on: 15
  off: 0
```

```json
{
  "lamp": {
    "on": 15,
    "off": 0
  }
}
```
<!-- langtabs-end -->

~~~

~~~admonish example "Cycling lamp"
<!-- langtabs-start -->
```yml
lamp:
  cycle: [0, 2, 4, 6, 8, 10, 12, 14]
```

```json
{
  "lamp": {
    "cycle": [0, 2, 4, 6, 8, 10, 12, 14]
  }
}
```
<!-- langtabs-end -->

~~~

---

## `waxable` behaviour

Allows the block to be 'waxed' using honeycomb or with filament items that have the "wax" behaviour.

~~~admonish info "Configurable Fields"
- `replacement`: The identifier of the block to replace the current block with. Example: `minecraft:waxed_copper_block`
~~~

---

## `place_on_water` behaviour

Allows the block to be placed on water.
Use this behaviour together with the `can_survive` behaviour to recreate lily-pads!


No fields to configure.