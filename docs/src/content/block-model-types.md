# Block model types

`blockModelType` values are available through polymer and limited in number, since they re-use block-states that are visually the same on the client.

Those are availble, and their numbers:

blockModelType | Amount of blocks
---|---
transparent_block_waterlogged | 52
farmland_block | 5
biome_plant_block | 15
biome_transparent_block_waterlogged | 65
transparent_block | 52
kelp_block | 25
cactus_block | 15
vines_block | 100
biome_transparent_block | 78
full_block | 1148
plant_block | 6

filament adds a few more block model types for tripwire and slabs:

blockModelType | Amount of blocks
---|---
flat_tripwire_block | 16
tripwire_block | 16
slab_block | 5

*It's important to set the `type` property in the block config to `slab` when using `slab_block` as `blockModelType`, see [block types](block-types.md) for more information*


When choosing blocks that break instantly on the client, like plant blocks, the destroyTime property in the block config has to be 0 as well