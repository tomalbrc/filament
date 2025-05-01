# Block model types

`blockModelType` values are available through polymer and limited in number, since they re-use block-states that are visually the same on the client.

The following options are availble, with their amount:

| blockModelType                      | Amount of blocks |
|-------------------------------------|------------------|
| full_block                          | 1149             |
| transparent_block                   | 52               |
| transparent_block_waterlogged       | 52               |
| biome_transparent_block             | 78               |
| biome_transparent_block_waterlogged | 65               |
| farmland_block                      | 5                |
| vines_block                         | 100              |
| plant_block                         | 8                |
| biome_plant_block                   | 15               |
| kelp_block                          | 25               |
| cactus_block                        | 15               |
| sculk_sensor_block                  | 150              |
| sculk_sensor_block_waterlogged      | 150              |
| tripwire_block                      | 32               |
| tripwire_block_flat                 | 32               |
| top_slab                            | 5                |
| top_slab_waterlogged                | 5                |
| bottom_slab                         | 5                |
| bottom_slab_waterlogged             | 5                |
| top_trapdoor                        | 21               |
| bottom_trapdoor                     | 21               |
| north_trapdoor                      | 21               |
| east_trapdoor                       | 21               |
| south_trapdoor                      | 21               |
| west_trapdoor                       | 21               |
| top_trapdoor_waterlogged            | 21               |
| bottom_trapdoor_waterlogged         | 21               |
| north_trapdoor_waterlogged          | 21               |
| east_trapdoor_waterlogged           | 21               |
| south_trapdoor_waterlogged          | 21               |
| west_trapdoor_waterlogged           | 21               |
| north_door                          | 168              |
| east_door                           | 168              |
| south_door                          | 168              |
| west_door                           | 168              |
| top_scaffolding                     | 168              |
| bottom_scaffolding                  | 168              |
| top_scaffolding_waterlogged         | 168              |
| bottom_scaffolding_waterlogged      | 168              |


BIOME_TRANSPARENT_BLOCK_WATERLOGGED = 6
FARMLAND_BLOCK = 5
VINES_BLOCK = 100
PLANT_BLOCK = 8
BIOME_PLANT_BLOCK = 15
KELP_BLOCK = 25
CACTUS_BLOCK = 15
SCULK_SENSOR_BLOCK = 150
SCULK_SENSOR_BLOCK_WATERLOGGED = 150
TRIPWIRE_BLOCK = 32
TRIPWIRE_BLOCK_FLAT = 32
TOP_SLAB = 5
TOP_SLAB_WATERLOGGED = 5
BOTTOM_SLAB = 5
BOTTOM_SLAB_WATERLOGGED = 5
TOP_TRAPDOOR = 21
BOTTOM_TRAPDOOR = 21
NORTH_TRAPDOOR = 21
EAST_TRAPDOOR = 21
SOUTH_TRAPDOOR = 21
WEST_TRAPDOOR = 21
TOP_TRAPDOOR_WATERLOGGED = 21
BOTTOM_TRAPDOOR_WATERLOGGED = 21
NORTH_TRAPDOOR_WATERLOGGED = 21
EAST_TRAPDOOR_WATERLOGGED = 21
SOUTH_TRAPDOOR_WATERLOGGED = 21
WEST_TRAPDOOR_WATERLOGGED = 21
NORTH_DOOR = 168
EAST_DOOR = 168
SOUTH_DOOR = 168
WEST_DOOR = 168
TOP_SCAFFOLDING = 7
BOTTOM_SCAFFOLDING = 6
TOP_SCAFFOLDING_WATERLOGGED = 7
BOTTOM_SCAFFOLDING_WATERLOGGED = 6

See the [polymer documentation](https://polymer.pb4.eu/latest/polymer-blocks/basics/) for more infos about the properties of the block model types

When choosing blocks that break instantly on the client, like plant_block or tripwire_block for example, the destroyTime property in the block config has to be 0 as well.

You can map the `blockModelType` field of block configs to blockstates, this allows you to change the hitbox of the block depending on the block-state.

In some cases, for example when using the `simple_waterloggable` behaviour, you might want to specify the waterlogged state for your custom block.

~~~admonish example
```json
{
  "id": "mynamespace:half_slab",
  "blockTags": ["minecraft:climbable"],
  "blockResource": {
    "models" : {
      "waterlogged=false": "minecraft:custom/block/dirt/dirt_slab",
      "waterlogged=true": "minecraft:custom/block/dirt/dirt_slab"
    }
  },
  "itemResource": {
    "models" : {
      "default": "minecraft:custom/block/dirt/dirt_slab"
    }
  },
  "behaviour": {
    "simple_waterloggable": {}
  },
  "blockModelType": {
    "waterlogged=false": "sculk_sensor_block",
    "waterlogged=true": "sculk_sensor_block_waterlogged"
  },
  "properties": {
    "blockBase": "minecraft:dirt"
  }
}
```
~~~

As of filament 0.16 it is possible to flag the block as virtual, to allow for an unlimited amount of block states. See [Blocks](blocks.md) for details.