# Third party compatibility

Filament has 2 options to make 
1. Decoration `blocks` and `seat` rotations (`seat` behaviour) more similar to that of Oraxen / ItemsAdder - filaments placement for those is rotated by 180° b default.
2. Change the display of cosmetics on the player, this uses the "head" as display context of the item. It also moves the backpack up, since filament uses item display entities instead of armor stands.

Those 2 options in filament.json are:
- `alternative_block_placement`
- `alternative_cosmetic_placement`

While filament does not support reading YAML files of third party mods/plugins, it should be possible to write a conversion script.