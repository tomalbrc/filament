# Filament: Custom Content Adder for Fabric Servers

[![Documentation](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/documentation/generic_64h.png)](https://tomalbrc.de/docs/filament)
[![Modrinth](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/modrinth_64h.png)](https://modrinth.com/mod/filament)
[![CurseForge](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/curseforge_64h.png)](https://www.curseforge.com/minecraft/mc-mods/filament)
[![GitHub](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/github_64h.png)](https://github.com/tomalbrc/filament)

[![discord](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/social/discord-plural_64h.png)](https://discord.gg/9X6w2kfy89)


filament simplifies adding custom content to Fabric-based Minecraft servers. It allows server owners to add new items, blocks, decorations and even mobs using JSON files—no coding required. Configure a block, item, decoration, or armor, and you're ready to go!

Clients can connect to servers using filament without installing any mods, as it relies on a resource pack, item display entities, and packet-based methods.

Decorations and other features have minimal server performance impact due to their packet-based nature.

**Use [Polymer's auto-hosting capability](https://polymer.pb4.eu/latest/user/resource-pack-hosting/) to host the resource pack—no third-party server required.**

**TLDR:** ItemsAdder/Oraxen alternative for Fabric.

## Features
- **Custom Items:** Add items with unique behaviors, including:
  - Armor
  - Cosmetics
  - Items running commands
  - Bows/Crossbows


- **Custom Blocks:** Add your own custom blocks like:
  - Trapdoors
  - Doors
  - Crops
  - Budding blocks


- **Custom Decorations:** Add furniture and decorations with configurable placement and interactions:
  - Benches
  - Showcases/Item-Displays
  - Container
  - Wearable & placeable backpacks

- **Custom Mobs:** Add your own mobs with simple goal-based AI - supports custom models too! Create:
  - Animals
  - Hostile mobs
  - Supports custom attributes
  - Control spawn chances and biomes to spawn in

## Installation
Requires [Fabric API](https://modrinth.com/mod/fabric-api) and [Polymer](https://modrinth.com/mod/polymer).  
Download filament and drop the `example_datapack` into the `datapacks` folder of your server world. filament uses Polymer's resource pack generation and hosting features.

## Usage
An example datapack is available in the GitHub repo. Just drop it into your world's datapacks folder.  
[Check out the documentation!](https://tomalbrc.de/projects/filament/docs)

## Mods using filament
These mods & datapacks demonstrate filament's capabilities:
- [Hat Club by MJRamon](https://modrinth.com/datapack/hat-club)
- [Backported Shelves by palm1](https://modrinth.com/datapack/backported-shelves)
- [Planked Chests by palm1](https://modrinth.com/datapack/planked-chests)
- [Toms Server Additions: Decorations & Furniture](https://modrinth.com/mod/tsa-decorations)
- [Toms Server Additions: Stone!](https://modrinth.com/mod/tsa-stone)
- [Toms Server Additions: Planks!](https://modrinth.com/mod/tsa-planks)
- [Toms Server Additions: Concrete!](https://modrinth.com/mod/tsa-concrete)

Archived:
- [Decorative Boxes by swzo](https://modrinth.com/mod/decorative-boxes)
- [Better Amethyst Polymerized by swzo](https://modrinth.com/mod/better-amethyst-polymerized)

## Credits
This project includes code from [FactoryTools](https://github.com/Patbox/FactoryTools) by Patbox, specifically the Virtual Destroy stages for decorations.  
A copy of the FactoryTools LGPL3 license is included in `factorytools-license.txt`.
