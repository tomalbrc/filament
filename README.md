# Filament: Custom Content adder for Fabric Servers

Filament simplifies the process of adding custom content to Fabric based Minecraft servers. 
It allows server owners to add new items, blocks, and decorations without having to write a single line of code. Everything is handled via json files, just configure a block, item, decoration or armor and you are ready to go!

Clients connecting to servers using filament don't have to install any mods, they can join using vanilla clients.
This is made possible using a resourcepack and item display entities.

Uses polymer autohost to host the resourcepack, no 3rd party server needed!

## Features

- **Custom Items:** Add unique items to the game with various behaviors, including armor, shooting, dyeing, and more.
- **Custom Blocks:** Introduce new blocks with different properties
- **Custom Decorations:** Introduce your own decorations such as furniture, complete with customizable placement and various behaviors for player interaction.

## Installation

Requires [Fabric-API](https://modrinth.com/mod/fabric-api) and [Polymer](https://modrinth.com/mod/polymer)

Use one of the releases on GitHub and drop the `example_datapack` into your server's worlds' `datapacks` folder.

filament uses polymers resource-pack generation to generate the RP and autohost features to automatically create a http server to serve the RP to your players! 

## Usage

There is an example data-pack in the GitHub repo, drop that into your worlds datapacks folder.

[Checkout the documentation!](https://tomalbrc.de/projects/filament/docs)

---

This project uses code from the [FactoryTools](https://github.com/Patbox/FactoryTools) project by Patbox, specifically the Virtual Destroy stages for decorations.

You can find a copy of the FactoryTools LGPL3 License in `factorytools-license.txt`

---

- [Modrinth](https://modrinth.com/mod/filament)
- [GitHub](https://github.com/tomalbrc/filament)
- [Discord](https://discord.gg/9X6w2kfy89)
