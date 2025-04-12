# Entity Goals

Entity goals define AI behaviors for your entity.

Use the `"goal"` field to specify the entities goals. Vanilla Minecraft uses 2 systems for entity AI, Brains and Goals.
Filament currently only supports the goal system.

All AI goals have a priority, lower priority = higher chance for the goal to run.

~~~admonish example
```json
{
  "look_at_mob_goal": {
    "priority": 1,
    "target": "minecraft:player",
    "look_distance": 8.0,
    "probability": 0.02,
    "only_horizontal": true
  }
}
```
~~~

---

## `float_goal`

Entity stays afloat in water.

~~~admonish info "Configurable Fields"
- `priority`: Determines execution order. Lower runs earlier.
~~~

---

## `look_at_mob_goal`

Makes the entity look at nearby entities of a specific type.

~~~admonish info "Configurable Fields"
- `priority`: Determines execution order.
- `target`: The entity type to look at (e.g. `"minecraft:player"`).
- `look_distance`: Max distance within which the target is visible.
- `probability`: Chance per tick to start looking. From `0.0` to `1.0`.
- `only_horizontal`: If `true`, the entity only rotates horizontally.
~~~

---

## `melee_attack_goal`

Makes the entity perform melee attacks on its target.

~~~admonish info "Configurable Fields"
- `priority`: Determines execution order.
- `speed_modifier`: Movement speed multiplier.
- `following_target_even_if_not_seen`: If `true`, the entity keeps following even if it loses sight.
~~~

---

## `move_through_village_goal`

Allows the entity to navigate through villages.

~~~admonish info "Configurable Fields"
- `priority`: Determines execution order.
- `speed_modifier`: Movement speed multiplier.
- `only_at_night`: If `true`, goal activates only at night.
- `distance_to_poi`: Max distance to a point of interest.
- `can_deal_with_doors`: If `true`, allows door interaction.
~~~

---

## `random_look_around_goal`

Entity randomly looks around occasionally.

~~~admonish info "Configurable Fields"
- `priority`: Determines execution order.
~~~

---

## `remove_block_goal`

Allows the entity to remove blocks of a certain type.

~~~admonish info "Configurable Fields"
- `priority`: Determines execution order.
- `block`: The block to be removed (as blockstate identifier).
- `speed_modifier`: Movement speed multiplier.
- `vertical_search_range`: How far vertically the entity looks for blocks to remove.
~~~

---

## `water_avoiding_random_stroll_goal`

Like `random_stroll`, but avoids walking into water.

~~~admonish info "Configurable Fields"
- `priority`: Determines execution order.
- `speed_modifier`: Movement speed multiplier.
- `probability`: Chance per tick to begin strolling.
~~~

---

## `defend_village_goal`

Triggers when hostile mobs threaten a village.

~~~admonish info "Configurable Fields"
- `priority`: Determines execution order.
~~~

---

## `hurt_by_target_goal`

Targets and attacks entities that have hurt this entity.

~~~admonish info "Configurable Fields"
- `priority`: Determines execution order.
- `ignore_from`: List of entity types that are ignored even if they cause damage.
- `alert_others`: List of entities to alert when this entity is attacked.
~~~

---

## `nearest_attackable_target_goal`

Targets the nearest valid entity.

~~~admonish info "Configurable Fields"
- `priority`: Determines execution order.
- `target`: Entity type to target (e.g., `"minecraft:player"`).
- `random_interval`: Delay between reevaluations.
- `must_see`: If `true`, entity must see the target.
- `must_reach`: If `true`, entity must be able to reach the target.
- `ignore_baby`: Ignores baby versions of the entity type.
- `ignore_in_water`: Ignores targets in water.
~~~
