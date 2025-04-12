# Entity Properties

Entity properties

Example:
```json
{
  "properties": {
    "size": [0.6, 1.95],
    "category": "monster",
    "sounds": {
      "ambient": "minecraft:entity.zombie.ambient",
      "swim": "minecraft:entity.generic.swim",
      "swimSplash": "minecraft:entity.generic.splash",
      "hurt": "minecraft:entity.zombie.hurt",
      "death": "minecraft:entity.zombie.death",
      "fall": {
        "small": "minecraft:entity.generic.small_fall",
        "big": "minecraft:entity.generic.big_fall"
      }
    },
    "ambient_sound_interval": 120,
    "xp_reward": 10,
    "is_sun_sensitive": true,
    "can_pickup_loot": true,
    "should_despawn_in_peaceful": false,
    "invulnerable": true,
    "fire_immune": true,
    "no_physics": true,
    "no_save": true,
    "no_summon": true,
    "can_use_portal": true
  }
}
```

### `size`:

Specifies the dimensions of the entity's bounding box. Should be a list of two floats: `[width, height]`.

### `category`:

Defines the entity's mob category. Valid values include `monster`, `creature`, `ambient`, `water_creature`.

### `sounds`:

Defines the set of sounds for the entity.

The structure:
```json
{
  "ambient": "namespace:sound",
  "swim": "namespace:sound",
  "swimSplash": "namespace:sound",
  "hurt": "namespace:sound",
  "death": "namespace:sound",
  "fall": {
    "small": "namespace:sound",
    "big": "namespace:sound"
  }
}
```

### `ambient_sound_interval`:

Number of ticks between ambient sounds.  
(20 ticks = 1 second)

Defaults to `80`

### `xp_reward`:

Amount of experience dropped when the entity dies.

Defaults to `5`

### `is_sun_sensitive`:

If `true`, the entity will start burning in sunlight (like zombies or skeletons).

Defaults to `false`

### `can_pickup_loot`:

If `true`, allows the entity to pick up items.

Defaults to `false`

### `should_despawn_in_peaceful`:

If `true`, the entity will despawn in Peaceful difficulty.

Defaults to `true`

### `invulnerable`:

If `true`, the entity cannot take damage from any source.

Defaults to `false`

### `fire_immune`:

If `true`, the entity is immune to fire and lava.

Defaults to `false`

### `no_physics`:

If `true`, the entity ignores gravity and physical interactions.

Defaults to `false`

### `no_save`:

If `true`, the entity is not saved to disk when the world is saved.

Defaults to `false`

### `no_summon`:

If `true`, the entity cannot be summoned using spawn eggs or commands.

Defaults to `false`

### `can_use_portal`:

If `true`, allows the entity to use Nether portals.

Defaults to `false`
