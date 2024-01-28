# filament
The usage part of this readme is outdated

## Description

This mod enhances the Minecraft gameplay experience by allowing you to add custom items, blocks, and decorations to the game. 
Please be aware that using a resource pack is necessary!

## Features

- **Custom Items:** Add unique items to the game with various behaviors, including armor, shooting, dyeing, and more.
- **Custom Blocks:** Introduce new blocks with different hitboxes and properties to enrich your world's visual and gameplay aspects.
- **Custom Decorations:** Introduce your own decorations such as furniture, complete with customizable placement and various behaviors for player interaction.

## Installation

1. Install [Minecraft Fabric](https://fabricmc.net/use/) and [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api)
2. Download the latest version of the mod from [Releases](https://github.com/tomalbrc/filament/releases).
3. Place the downloaded `.jar` file into the `mods` folder of your server's installation directory.

## Usage

### Custom Items

Custom items are defined using JSON files. 
Here's an example of an item configuration:

```json
{
  "id": "myIdentifier:amethyst_helmet",
  "vanillaItem": "minecraft:leather_helmet",
  "properties": {
    "durability": 10,
    "stackSize": 1
  },
  "behaviour": {
    "armor": {
      "attributes": {
        "name": "attribute.maxhealth",
        "value": 1
      },
      "slot": "head",
      "texture": "minecraft:amethyst"
    }
  }
}
```

Items can have multiple behaviors like "armor" for wearable armor, "shoot" for shooting, "dye" for tinting, and more.

## Custom Blocks
Custom blocks are defined similarly using JSON files. 
Here's an example of a block configuration:

```
{
  "id": "myIdentifier:crystalite",
  "models": {
    "default": "minecraft:custom/block/crystalite/crystalite"
  },
  "itemModel": "minecraft:custom/block/crystalite/crystalite",
  "type": "transparent_flat",
  "properties": {
    "blockBase": "minecraft:amethyst_cluster",
    "itemBase": "minecraft:paper",
    "destroyTime": 0,
    "explosionResistance": 0,
    "lightEmission": 15
  }
}
```

## Custom Decorations
Define decorations using JSON files.
Here's an example of a decoration configuration:

```
{
  "id": "myIdentifier:gravestone",
  "model": "minecraft:custom/furniture/gravestone",
  "itemModel": "minecraft:custom/furniture/gravestone",
  "properties": {
    "rotate": true,
    "rotateSmooth": true
  },
  "barriers": [[0, 0, 0]]
}
```

`rotate`: Allows furniture to be rotated in 90° steps during placement.

`rotateSmooth`: Enables rotation in 45° intervals.

`model`: Path to the model in the resource pack.

When `barriers` are specified, collision blocks (barrier blocks) will be placed at the specified location.

