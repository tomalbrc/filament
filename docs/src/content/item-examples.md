# Examples

See the datapack in the GitHub repository for more examples!

https://github.com/tomalbrc/filament/tree/main/example_datapack

~~~admonish tip
All fields support either camelCase or snake_case!
~~~

~~~admonish example
### Clown horn intrument:
**JSON**:
```json
{
  "id": "mynamespace:clown_horn",
  "vanillaItem": "minecraft:paper",
  "itemResource":  {
    "models": {
      "default": "mynamespace:custom/misc/clown_horn"
    }
  },
  "properties": {
    "stackSize": 1
  },
  "behaviour": {
    "instrument": {
      "sound": "mynamespace:misc.honk",
      "range": 64,
      "useDuration": 60
    }
  }
}
```

**YAML**:
```yaml
id: mynamespace:clown_horn
vanilla_item: minecraft:paper
item_resource:
  models:
    default: mynamespace:custom/misc/clown_horn
properties:
  stack_size: 1
behaviour:
  instrument:
    sound: mynamespace:misc.honk
    range: 64
    use_duration: 60
```

### Allay trap:
**JSON**:
```json
{
  "id": "mynamespace:allay_bottle",
  "vanillaItem": "minecraft:carrot_on_a_stick",
  "itemTags": ["enchantable/durability"],
  "itemResource": {
    "models": {
      "default": "mynamespace:custom/traps/allay_bottle",
      "trapped": "mynamespace:custom/traps/allay_bottle_trapped"
    }
  },
  "properties": {
    "durability": 20,
    "stackSize": 1
  },
  "behaviour": {
    "trap": {
      "types": ["minecraft:allay"],
      "useDuration": 120
    }
  }
}
```

**YAML**:
```yaml
id: mynamespace:allay_bottle
vanilla_item: minecraft:carrot_on_a_stick
item_resource:
  models:
    default: mynamespace:custom/traps/allay_bottle
    trapped: mynamespace:custom/traps/allay_bottle_trapped
properties:
  durability: 20
  stack_size: 1
behaviour:
  trap:
    types:
      - minecraft:allay
    use_duration: 120
```

### Hat (can be put into inventory/swapped like normal helmets)
**JSON**:
```json
{
  "id": "mynamespace:magic_hat",
  "vanillaItem": "minecraft:paper",
  "itemResource": {
    "models": {
      "default": "minecraft:custom/hats/magic_hat"
    }
  },
  "properties": {
    "stackSize": 1
  },
  "components": {
    "minecraft:equippable": {
      "slot": "head",
      "swappable": true,
      "damage_on_hurt": true,
      "equip_on_interact": false
    }
  }
}
```

**YAML**:
```yaml
id: mynamespace:magic_hat
vanilla_item: minecraft:paper
item_resource:
  models:
    default: minecraft:custom/hats/magic_hat
properties:
  stack_size: 1
components:
  minecraft:equippable:
    slot: head
    swappable: true
    damage_on_hurt: true
    equip_on_interact: false
```
~~~