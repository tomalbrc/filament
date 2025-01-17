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
| plant_block                         | 7                |
| biome_plant_block                   | 15               |
| kelp_block                          | 25               |
| cactus_block                        | 15               |
| tripwire_block                      | 32               |
| tripwire_block_flat                 | 32               |
| top_slab                            | 5                |
| top_slab_waterlogged                | 5                |
| bottom_slab                         | 5                |
| bottom_slab_waterlogged             | 5                |

See the [polymer documentation](https://polymer.pb4.eu/latest/polymer-blocks/basics/) for more infos about the properties of the block model types

When choosing blocks that break instantly on the client, like plant_block or tripwire_block for example, the destroyTime property in the block config has to be 0 as well.

You can map the `blockModelType` field of block configs to blockstates, this allows you to change the hitbox of the block depending on the block-state.

In some cases, for example when using the `simple_waterloggable` behaviour, you might want to specify the waterlogged state for your custom block.

Example:
```json
{
  "id": "mynamespace:half_slab",
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