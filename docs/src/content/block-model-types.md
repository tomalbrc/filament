# Block model types

`blockModelType` values are available through polymer and limited in number, since they re-use block-states that are visually the same on the client.

The following options are availble, with their amount:

blockModelType | Amount of blocks
---|---
full_block | 1149
transparent_block | 52
transparent_block_waterlogged | 52
biome_transparent_block | 78
biome_transparent_block_waterlogged | 65
farmland_block | 5
vines_block | 100
plant_block | 7
biome_plant_block | 15
kelp_block | 25
cactus_block | 15
tripwire_block | 32
tripwire_block_flat | 32
top_slab | 5
top_slab_waterlogged | 5
bottom_slab | 5
bottom_slab_waterlogged | 5


When choosing blocks that break instantly on the client, like plant blocks, the destroyTime property in the block config has to be 0 as well