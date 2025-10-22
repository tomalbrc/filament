# Entity Properties

Entity properties

Example:
```json
{
  "properties": {
    "size": [
      0.5,
      0.6
    ],
    "category": "creature",
    "xp_reward": 15,
    "is_sun_sensitive": false,
    "should_despawn_in_peaceful": true,
    "invulnerable": false,
    "fire_immune": false,
    "no_save": false,
    "no_summon": false,
    "no_physics": false,
    "can_use_portal": true,
    "can_be_leashed": true,
    "despawn_when_far_away": false,
    "offspring": "pig",
    "food": ["sugar_cane"],
    "ambient_sound_interval": 80,
    "sounds": {
      "ambient": "entity.armadillo.ambient",
      "hurt": "entity.armadillo.hurt",
      "death": "entity.armadillo.death",
      "swim_splash": "entity.generic.splash",
      "swim": "entity.generic.swim",
      "fall": {
        "small": "entity.generic.small_fall",
        "big": "entity.generic.big_fall"
      }
    }
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
