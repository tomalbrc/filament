## Creating a sword

Filament item definitions are placed in `data/<namespace>/filament/item/`.  

---

## Sword definition

### JSON

```json
{
  "id": "mynamespace:ruby_sword",
  "vanillaItem": "wooden_sword",
  "itemTags": ["swords"],
  "translations": {
    "en_us": "Ruby Sword"
  },
  "item_resource": {
    "parent": "item/handheld",
    "textures": {
      "default": {
        "layer0": "mynamespace:item/ruby_set/ruby_sword"
      }
    }
  },
  "components": {
    "minecraft:attribute_modifiers": [
      {
        "type": "minecraft:attack_damage",
        "amount": 5,
        "id": "minecraft:base_attack_damage",
        "operation": "add_value",
        "slot": "mainhand"
      },
      {
        "type": "minecraft:attack_speed",
        "amount": -2.4,
        "id": "minecraft:base_attack_speed",
        "operation": "add_value",
        "slot": "mainhand"
      }
    ],
    "minecraft:break_sound": "minecraft:entity.item.break",
    "minecraft:damage": 0,
    "minecraft:enchantable": {
      "value": 14
    },
    "minecraft:max_damage": 250,
    "minecraft:max_stack_size": 1,
    "minecraft:rarity": "common",
    "minecraft:tool": {
      "can_destroy_blocks_in_creative": false,
      "damage_per_block": 2,
      "rules": [
        {
          "blocks": "minecraft:cobweb",
          "correct_for_drops": true,
          "speed": 15
        },
        {
          "blocks": "#minecraft:sword_instantly_mines",
          "speed": 3.4028235e+38
        },
        {
          "blocks": "#minecraft:sword_efficient",
          "speed": 1.5
        }
      ]
    },
    "minecraft:weapon": {}
  }
}

```

---

## File location

### JSON

```text
data/mynamespace/filament/item/ruby_sword.json
```

---

## Assets structure

```text
MyDatapack/
├── pack.mcmeta
├── data/
│   └── mynamespace/
│       └── filament/
│           └── item/
│               └── ruby_sword.json
└── assets/
    └── mynamespace/
        ├── models/
        │   └── item/
        │       └── ruby_sword.json
        └── textures/
            └── item/
                └── ruby_sword.png
```

---

## Automatically generated model

`itemResource` defines the item model. 
Can be generated from the texture reference.

### Generated model

```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "mynamespace:item/ruby_sword"
  }
}
```

---

## Texture requirement

Only the texture is required if the model is generated:

```text
assets/mynamespace/textures/item/ruby_sword.png
```

---

## Giving the item

```mcfunction
/give @p mynamespace:ruby_sword
```



> [!TIP]
> This section of the documentation is incomplete. Contributions are welcome!
> Feel free to contribute on [GitHub](https://github.com/tomalbrc/filament).
> For now, use the example_datapack in the GitHub repo.

