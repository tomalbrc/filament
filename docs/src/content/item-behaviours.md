# Behaviours

Item behaviors define specific functionalities associated with items, blocks, and decorations.
All behaviours are optional, some are mutually exclusive (trap, shoot and intrument for example).

Example with all behaviours set:
```json
{
  "id": "mynamespace:clown_horn",
  "vanillaItem": "minecraft:paper",
  "models": {
    "default": "mynamespace:custom/misc/clown_horn"
  },
  
  "behaviour": {
    "instrument": {
      "sound": "mynamespace:misc.honk",
      "range": 64,
      "useDuration": 60
    },
    "shoot": {
      "consumes": false,
      "baseDamage": 2.0,
      "speed": 1.0,
      "projectile": "minecraft:iron_axe"
    },
    "armor": {
      "slot": "head",
      "texture": "mynamespace:texture_name"
    },
    "trap": {
      "types": ["minecraft:villager", "minecraft:zombie", "minecraft:skeleton"],
      "useDuration": 0
    },
    "fuel": {
      "value": 10
    },
    "food": {
      "hunger": 1,
      "saturation": 0.6,
      "meat": false
    }
  }
}
```

### `instrument`:

- `sound`: The sound associated with the instrument.
- `range`: The range of the instrument.
- `useDuration`: Delay in ticks for using the instrument.

### `shoot`:

- `consumes`: Indicates whether the shooting action consumes the item.
- `baseDamage`: The base damage of the projectile.
- `speed`: The speed at which the projectile is fired.
- `projectile`: The identifier for the projectile item.

### `armor`:

- `slot`: The equipment slot for the armor piece (e.g., head, chest, legs, or feet).
- `texture`: The resource location of the texture armor , used by fancypants (via polymer).

### `trap`:

- `types`: List of allowed entity types to trap.
- `useDuration`: Delay in ticks for using the trap.

### `fuel`:

- `value`: Value of the fuel, used in furnaces and similar item burning blocks.

### `food`:

- `hunger`: The amount of hunger restored when consumed.
- `saturation`: The saturation modifier provided by the food.
- `meat`: Boolean (true/false) indicating whether the food is classified as meat.
