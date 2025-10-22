# Creating custom armor

Custom armor requires an entity equipment "model" json in your resource-pack as well as 2 textures and the `minecraft:equippable` component.

## Model and textures

The model json should be located in `assets/<namespace>/equipment/<name>.json` with the following contents:
```json
{
  "layers": {
    "humanoid": [
      {
        "texture": "<namespace>:<texture-name>"
      }
    ],
    "humanoid_leggings": [
      {
        "texture": "<namespace>:<texture-name>"
      }
    ]
  }
}
```

The `humanoid` entry is used for the helmet and chestplate, `humanoid_leggings` for leggings and boots.

While this tutorial focuses on humanoid armor worn by players and other humanoid mobs, you can also create custom armor /harnesses for happy ghasts, wolf armor, elytras, and more.
[See the vanilla model files as reference](https://mcasset.cloud/1.21.7/assets/minecraft/equipment/).

The location of the textures is partially based on the layers of the model json:
`assets/<namespace>/textures/entity/<layer-name>/<texture-name>.png`

Let's use this json as example (`assets/mynamespace/equipment/obsidian.json`):
```json
{
  "layers": {
    "humanoid": [
      {
        "texture": "mynamespace:obsidian"
      }
    ],
    "humanoid_leggings": [
      {
        "texture": "mynamespace:obsidian"
      }
    ]
  }
}
```

In this case the game expect the textures to be located in:

`assets/mynamespace/textures/entity/humanoid/obsidian.png`

and

`assets/mynamespace/textures/entity/humanoid_leggings/obsidian.png`

[See the vanilla texture files as reference](https://mcasset.cloud/1.21.7/assets/minecraft/textures/entity/equipment/humanoid).

You will also need an icon for the item itself.

## Filament json

The filament item json will need the `minecraft:equippable` and attribute modifiers.
We will use the name of the json file and our namespace as the asset_id;
`mynamespace:obsidian` will use the file `assets/mynamespace/equipment/obsidian.json`

```json
{
  "id": "server:obsidian_chestplate",
  "vanillaItem": "iron_chestplate",
  "itemTags": ["chest_armor"],
  "translations": {
    "en_us": "Obsidian Chestplate"
  },
  "item_resource": {
    "parent": "item/generated",
    "textures": {
      "default": {
        "layer0": "mynamespace:item/obsidian_set/obsidian_chestplate"
      }
    }
  },
  "behaviour": {
    "generate_trim_models": {}
  },
  "components": {
    "attribute_modifiers": [
      {
        "type": "armor",
        "amount": 6,
        "id": "armor.chestplate",
        "operation": "add_value",
        "slot": "chest"
      },
      {
        "type": "armor_toughness",
        "amount": 0,
        "id": "armor.chestplate",
        "operation": "add_value",
        "slot": "chest"
      }
    ],
    "break_sound": "entity.item.break",
    "damage": 0,
    "enchantable": {
      "value": 6
    },
    "equippable": {
      "asset_id": "mynamespace:obsidian",
      "equip_sound": "item.armor.equip_iron",
      "slot": "chest"
    },
    "max_damage": 210,
    "max_stack_size": 1,
    "rarity": "common",
    "repair_cost": 0
  }
}
```

You can look-up the default components of vanilla items [here](https://far.ddns.me/item?ver=1.21.10)

The `generate_trim_models` behaviour will automatically generate the item models for the different armor trims and add them to the resource-pack.
