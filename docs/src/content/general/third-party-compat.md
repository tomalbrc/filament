# Third party compatibility

# Oraxen and Nexo

There are 2 options in `config/filament/general.json` to make: 
1. Decoration `blocks` and `seat` rotations (`seat` behaviour) more similar to that of Oraxen / Nexo / ItemsAdder - filaments placement for those is rotated by 180° by default.
2. Change the display of cosmetics on the player, this uses the "head" as display context of the item. It also moves the backpack up, since filament uses item display entities instead of armor stands.

Those 2 options in `config/filament/general.json` are:
- `alternative_block_placement`
- `alternative_cosmetic_placement`

While filament does not support reading YAML files of third party mods/plugins, it should be possible to write a conversion script.

Some older itemsadder resourcepacks use the armor stand head display contexts for the models. Make sure to set `display` to `head` in the decorations' properties to properly display them.

# WorldEdit

Everything should be fully compatible, the only known bug is decorations not appearing or being removed when cloning or moving an area. This can be fixed by a server restart

---

# Polymer - Quality of Life (polymer-qol)

A **client + server** mod by **DrexHD** that provides quality-of-life improvements for players on Polymer-powered servers. **Must be installed on both the client and server to work**.

Features:
- **Client-side mining** - uses Polymer's block syncing to calculate modded block breaking times on the client, allowing for smoother block breaking with high efficiency tools with Haste 2 effect (especially useful on high ping)
- **No block updates** - breaking and placing blocks won't cause block updates on the client, removing visually distracting incorrect blocks
- **Preventing arm swing** - players won't swing their arms when interacting with fake noteblocks (used by custom blocks)

Server config at `config/polymer-qol-server.json` allows you to disable individual features.

**Download:**
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/polymer-quality-of-life)
- [Modrinth](https://modrinth.com/mod/polymer-qol)

---

# Smoother Server Cosmetics

A **client-side** mod by **AmoAsterVT** that smooths the rotation of server-side cosmetics, removing the visible delay when turning. Only needs to be installed on the **client**, no server-side changes required.

**Download:**
- [Modrinth](https://modrinth.com/mod/smoother-server-cosmetics)
