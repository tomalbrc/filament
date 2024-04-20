# Examples

### Clown horn intrument:
```json
{
  "id": "mynamespace:clown_horn",
  "vanillaItem": "minecraft:paper",
  "models": {
    "default": "mynamespace:custom/misc/clown_horn"
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

### Allay trap:
```json
{
  "id": "mynamespace:allay_bottle",
  "vanillaItem": "minecraft:carrot_on_a_stick",
  "models": {
    "default": "mynamespace:custom/traps/allay_bottle",
    "trapped": "mynamespace:custom/traps/allay_bottle_trapped"
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

### Hat (can only be equipped using commands or 3rd party mods)
```json
{
  "id": "mynamespace:magic_hat",
  "vanillaItem": "minecraft:paper",
  "models": {
    "default": "mynamespace:custom/hats/magic_hat"
  },
  "properties": {
    "durability": 1,
    "stackSize": 1
  }
}
```
