# Filament: Custom Content Adder for Fabric Servers

Filament simplifies the process of adding custom content to Fabric-based Minecraft servers.

It allows server owners or content creators to add new items, blocks, and decorations without writing a single line of code. Everything is data driven via json files, just configure a block, item, decoration or armor and you are ready to go!

Clients connecting to servers using Filament don't need to install any mods, they can connect using vanilla clients.

This is made possible by a resource pack, item display entities, and other packet-based tricks.

Since most things are packet based, especially decorations, the performance impact on the server is very low.

You should use [polymers auto-host capability](https://polymer.pb4.eu/latest/user/resource-pack-hosting/) to host the resourcepack, no 3rd party server needed!

TLDR: ItemsAdder / Oraxen alternative for fabric. Doesn't handle datapack files such as item-tags / block-tags, etc (bring your own datapack).

## Features

- **Custom Items:** Add unique items to the game with different behaviors, including armor, shooting, and more.
- **Custom Blocks:** Introduce new blocks with different properties.
- **Custom Decorations:** Introduce your own decorations such as furniture, complete with customizable placement and various behaviors for player interaction.

## Installation

Requires [Fabric API](https://modrinth.com/mod/fabric-api) and [Polymer](https://modrinth.com/mod/polymer)

Use one of the releases on GitHub and drop the `example_datapack` into the `datapacks` folder of your server world.

filament uses polymers resource-pack generation to generate the RP and autohost features to automatically create an http server to serve the RP to your players!

## Usage

There is an example datapack in the GitHub repo, just drop it into your world's datapacks folder.

[Check out the documentation!](https://tomalbrc.de/projects/filament/docs)

---

This project uses code from the [FactoryTools](https://github.com/Patbox/FactoryTools) project by Patbox, specifically the Virtual Destroy stages for decorations.

You can find a copy of the FactoryTools LGPL3 License in `factorytools-license.txt`

---

- [Modrinth](https://modrinth.com/mod/filament)
- [GitHub](https://github.com/tomalbrc/filament)
- [Discord](https://discord.gg/9X6w2kfy89)
