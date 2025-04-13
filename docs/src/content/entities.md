# Entities

Filament allows you to add simple entities with custom attributes, custom blockbench model with animations and some AI goals from the vanilla game.

~~~admonish info
The entity module is experimental and fields will change in between versions!

You can disable the module in filaments config if you run into issues.
~~~

## File Location

Item configuration files should be placed in the following directory:
```
MyDatapack/data/<namespace>/filament/entity/myentity.json
```

## Example

Here is a basic example for a zombie-like entity with an Iron Golem model:
~~~admonish example
```json
{
  "id": "mynamespace:unfriendly_golem",
  "entity_tags": [
    "can_breathe_under_water"
  ],
  "entity_type": "iron_golem",
  "translations": {
    "en_us": "Unfriendly Golem"
  },
  "properties": {
    "size": [
      0.5,
      1.8
    ],
    "category": "monster",
    "xp_reward": 5,
    "is_sun_sensitive": true,
    "should_despawn_in_peaceful": true,
    "invulnerable": false,
    "fire_immune": false,
    "no_save": false,
    "no_summon": false,
    "no_physics": false,
    "can_use_portal": true,
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
  },
  "goals": [
    {
      "type": "remove_block_goal",
      "priority": 4,
      "block": "turtle_egg",
      "speed_modifier": 1.0,
      "vertical_search_range": 3
    },
    {
      "type": "look_at_mob_goal",
      "priority": 8,
      "target": "player",
      "look_distance": 8.0,
      "probability": 0.02
    },
    {
      "type": "random_look_around_goal",
      "priority": 8
    },
    {
      "type": "melee_attack_goal",
      "priority": 2,
      "speed_modifier": 1.0,
      "following_target_even_if_not_seen": false
    },
    {
      "type": "move_through_village_goal",
      "priority": 6,
      "speed_modifier": 1.0,
      "only_at_night": true,
      "distance_to_poi": 8,
      "can_deal_with_doors": true
    },
    {
      "type": "water_avoiding_random_stroll_goal",
      "priority": 7,
      "speed_modifier": 1.0,
      "probability": 0.001
    },
    {
      "type": "hurt_by_target_goal",
      "priority": 1,
      "alert_others": [
        "zombified_piglin"
      ]
    },
    {
      "type": "nearest_attackable_target_goal",
      "priority": 2,
      "target": "player",
      "speed_modifier": 1.0,
      "must_see": true,
      "must_reach": false
    },
    {
      "type": "nearest_attackable_target_goal",
      "priority": 3,
      "target": "villager",
      "speed_modifier": 1.0,
      "must_see": true,
      "must_reach": false
    },
    {
      "type": "nearest_attackable_target_goal",
      "priority": 5,
      "target": "iron_golem",
      "speed_modifier": 1.0,
      "must_see": true,
      "must_reach": false
    },
    {
      "type": "nearest_attackable_target_goal",
      "priority": 5,
      "target": "turtle",
      "speed_modifier": 1.0,
      "must_see": true,
      "must_reach": false
    }
  ],
  "attributes": {
    "minecraft:follow_range": 35,
    "minecraft:movement_speed": 0.23,
    "minecraft:attack_damage": 3.0,
    "minecraft:armor": 2.0,
    "minecraft:attack_range": 1.5,
    "minecraft:spawn_reinforcements": 0.5,
    "minecraft:step_height": 0
  },
  "spawn": {
    "weight": 40,
    "min_group_size": 2,
    "max_group_size": 4,
    "found_in_overworld": true,
    "found_in_nether": false,
    "found_in_end": false,
    "biomes": ["desert"],
    "biomeTags": ["c:desert"]
  }
}
```
~~~

## Model & Animations

The model and animations are controlled using the `animation` field:
```json
{
  "animation": {
    "model": "mynamespace:penguin",
    "walk_animation": "walk",
    "idle_animation": "idle"
  }
}
```

## Spawn Options

The field `spawn` controls where or how your entity spawned in the world.

- weight: Spawn weight. 10-50 are usually acceptable values 
- min_group_size: Min. amount of entities to spawn at once
- max_group_size: Max. amount of entities to spawn at once
- spawn_like: List of entity types to spawn alike
- biomes: List of biomes 
- biome_tags: List of biome tags
- found_in_overworld: Entity will only spawn in the overworld if set to true
- found_in_nether: Entity will only spawn in the nether if set to true
- found_in_end: Entity will only spawn in the end if set to true

~~~admonish info
The category of the entity can change its spawn behaviour. "monster" entities will only spawn in dark places/at night!  
~~~