# filament
The usage part of this readme is outdated
[Checkout the discord for more up to date info](https://discord.gg/9X6w2kfy89)

## Description

Add custom items, blocks, and decorations to your server. 
Uses polymer autohost to host the resourcepack, no 3rd party server needed!

## Features

- **Custom Items:** Add unique items to the game with various behaviors, including armor, shooting, dyeing, and more.
- **Custom Blocks:** Introduce new blocks with different properties
- **Custom Decorations:** Introduce your own decorations such as furniture, complete with customizable placement and various behaviors for player interaction.

## Installation

Needs fabric api and polymer

## Usage

### Custom Items

Custom items are defined using JSON files. 
Here's an example of an item configuration:

```json
{
  "id": "tsa:clown_horn",
  "vanillaItem": "minecraft:paper",
  "models": {
    "default": "minecraft:custom/misc/clown_horn"
  },
  "properties": {
    "stackSize": 1
  },
  "behaviour": {
    "instrument": {
      "sound": "tsa:misc.honk",
      "range": 64,
      "useDuration": 60
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
  "id": "tsa:buttercup",
  "models": {
    "default": "minecraft:custom/block/plants/buttercup"
  },
  "itemModel": "minecraft:custom/block/plants/buttercup",
  "type": "plant_block",
  "properties": {
    "destroyTime": 0,
    "blockBase": "minecraft:dandelion",
    "itemBase": "minecraft:paper"
  }
}
```

## Custom Decorations
Define decorations using JSON files.
Here's an example of a decoration configuration:

```
{
  "id": "tsa:darkoak_bench",
  "model": "minecraft:custom/furniture/benches/darkoak_bench",
  "itemModel": "minecraft:custom/furniture/benches/darkoak_bench",
  "properties": {
      "rotate": true,
      "rotateSmooth": true
  },
  "behaviour": {
    "seat": [
      {
        "offset": [0, 0.42, 0]
      },
      {
        "offset": [1, 0.42, 0]
      }
    ]
  },
  "blocks": [
    {
      "origin": [0,0,0],
      "size": [2,1,1]
    }
  ]
}
```

`rotate`: Allows furniture to be rotated in 90° steps during placement.

`rotateSmooth`: Enables rotation in 45° intervals.

`model`: Path to the model in the resource pack.

