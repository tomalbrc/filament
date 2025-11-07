# Skill System

MythicMobs-like skill system

### Trigger Reference

| Trigger ID         |
|--------------------|
| `ON_ATTACK`        |
| `ON_DAMAGED`       |
| `ON_SPAWN`         |
| `ON_DESPAWN`       |
| `ON_CHANGE_TARGET` |
| `ON_LOAD`          |
| `ON_SPAWN_OR_LOAD` |
| `ON_DEATH`         |
| `ON_TIMER`         |
| `ON_INTERACT`      |
| `ON_BREED`         |


### Mechanics Reference

| Mechanic ID           | Aliases / Short Names                                                |
|-----------------------|----------------------------------------------------------------------|
| `delay`               |                                                                      |
| `message`             |                                                                      |
| `potion`              |                                                                      |
| `skill`               |                                                                      |
| `set_variable`        |                                                                      |
| `sound`               |                                                                      |
| `ender_effect`        | `ender`, `effect:ender`, `e:ender`                                   |
| `particle`            |                                                                      |
| `particle_ring`       |                                                                      |
| `particle_line`       |                                                                      |
| `particle_line_helix` | `ParticleLineHelix`, `effect:particlelinehelix`, `particlehelixline` |
| `particle_sphere`     | `effect:particlesphere`, `e:ps`, `ps`                                |
| `feed`                |                                                                      |
| `ignite`              |                                                                      |


### Targeters Reference:

| Targeter ID          | Aliases / Short Names                                                                |
|----------------------|--------------------------------------------------------------------------------------|
| `target`             | `T`, `Target`                                                                        |
| `trigger`            | `Trigger`                                                                            |
| `vehicle`            | `Vehicle`                                                                            |
| `self`               | `Self`, `Caster`, `Boss`, `Mob`                                                      |
| `self_location`      | `SelfLocation`, `casterLocation`, `bossLocation`, `mobLocation`                      |
| `self_eye_location`  | `SelfEyeLocation`, `eyeDirection`                                                    |
| `players_in_radius`  | `PlayersInRadius`, `PIR`                                                             |
| `players_in_ring`    | `PlayersInRing`                                                                      |
| `players_in_world`   | `world`, `PlayersInWorld`                                                            |
| `tracked_players`    | `tracked`, `TrackedPlayers`                                                          |
| `owner`              | `Owner`                                                                              |
| `mobs_in_radius`     | `MobsInRadius`, `MIR`                                                                |
| `items_in_radius`    | `ItemsInRadius`, `IIR`                                                               |
| `entities_in_radius` | `EntitiesInRadius`, `livingEntitiesInRadius`, `livingInRadius`, `allInRadius`, `EIR` |
| `forward`            | `Forward`                                                                            |

### Conditions Reference

| Condition ID                     |
|----------------------------------|
| `altitude`                       |
| `biome`                          |
| `y_diff`                         |
| `block_type`                     |
| `is_filament_mob`                |
| `mounted`                        |
| `is_in_survival_mode`            |
| `blocking`                       |
| `food_level`                     |
| `string_not_empty`               |
| `food_saturation`                |
| `distance_from_tracked_location` |
| `is_invulnerable`                |
| `raining`                        |
| `players_online`                 |
| `bounding_boxes_overlap`         |
| `is_player`                      |
| `variable_contains`              |
| `string_empty`                   |
| `night`                          |
| `trigger_block_type`             |
| `damage_tag`                     |
| `is_leashed`                     |
| `target_in_line_of_sight`        |
| `trigger_item_type`              |
| `height_above`                   |
| `sunny`                          |
| `target_not_within`              |
| `looking_at`                     |
| `health_percentage`              |
| `motion_z`                       |
| `has_passenger`                  |
| `z_diff`                         |
| `motion_y`                       |
| `height_below`                   |
| `day`                            |
| `skill_on_cooldown`              |
| `line_of_sight`                  |
| `holding`                        |
| `entity_type`                    |
| `is_baby`                        |
| `is_caster`                      |
| `fall_speed`                     |
| `world`                          |
| `health`                         |
| `biome_type`                     |
| `light_level`                    |
| `on_ground`                      |
| `is_monster`                     |
| `dusk`                           |
| `x_diff`                         |
| `target_within`                  |
| `is_living`                      |
| `dawn`                           |
| `distance`                       |
| `sprinting`                      |
| `dimension`                      |
| `is_skill`                       |
| `inside`                         |
| `damage_amount`                  |
| `directional_velocity`           |
| `moving`                         |
| `on_block`                       |
| `target_not_in_line_of_sight`    |
| `thundering`                     |
| `has_free_inventory_slot`        |
| `metaskill`                      |
| `gliding`                        |
| `distance_from_location`         |
| `burning`                        |
| `world_time`                     |
| `wearing`                        |
| `is_climbing`                    |
| `name`                           |
| `height`                         |
| `chance`                         |
| `bow_tension`                    |
| `variable_is_set`                |
| `motion_x`                       |
| `vehicle_is_dead`                |
| `size`                           |
| `variable_in_range`              |
| `enchanting_experience`          |
| `outside`                        |
| `enchanting_level`               |
| `is_using_spyglass`              |
| `field_of_view`                  |
| `distance_from_spawn`            |
| `has_tag`                        |
| `block_type_in_radius`           |
| `yaw`                            |
| `has_item`                       |
| `velocity`                       |
| `has_offhand`                    |
| `crouching`                      |
| `variable_equals`                |
| `last_damage_cause`              |
