# Third party compatibility

# Oraxen and Nexo

Filament supports Nexo and Oraxen packs directly as of 0.17.8 (there is also 0.14.9 for 1.21.1 that supports this feature)

Create a folder called "nexo" in your games root directory and copy your oraxen or nexo packs into it.
```
└── mods/
└── nexo/
    └── your-pack/
        ├── items/
        └── pack/
```

The folder name of the pack should not contain spaces or special character, it is used as namespace in the identifier for your items and blocks:
`your-pack:your_item`

The assets will automatically be added to the generated resource-pack.

# Config options

There are 2 options in `filament.json` to make: 
1. Decoration `blocks` and `seat` rotations (`seat` behaviour) more similar to that of Oraxen / Nexo / ItemsAdder - filaments placement for those is rotated by 180° b default.
2. Change the display of cosmetics on the player, this uses the "head" as display context of the item. It also moves the backpack up, since filament uses item display entities instead of armor stands.

Those 2 options in filament.json are:
- `alternative_block_placement`
- `alternative_cosmetic_placement`

While filament does not support reading YAML files of third party mods/plugins, it should be possible to write a conversion script.

Some older itemsadder resourcepacks use the armor stand head display contexts for the models. Make sure to set `display` to `head` in the decorations' properties to properly display them.

# WorldEdit

Everything should be fully compatible, the only known bug is decorations not appearing or being removed when cloning or moving an area. This can be fixed by a server restart

---

Client-side mod for improved rotation (less delay) of cosmetics:
https://modrinth.com/mod/smoother-server-cosmetics
by AmoAsterVT!
