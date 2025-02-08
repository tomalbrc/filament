# Decoration Examples

> **Note**: All fields support either camelCase or snake_case!

### Animated chest with solid 1x1 solid collision:
```json5
{
  "id": "mynamespace:example_chest",
  "itemResource": {
    "models": {
      "default": "mynamespace:custom/furniture/chests/example_chest"
    }
  },
  "properties": {
    "rotate": true, // to allow 90° rotations
    "rotateSmooth": true // to allow 45° rotations
  },
  "behaviour": {
    "animation": {
      "model": "my_filament_namespace:example_chest"
    },
    "container": {
      "name": "Example Chest",
      "size": 36,
      "purge": false,
      "openAnimation": "open",
      "closeAnimation": "close"
    }
  },
  "blocks": [
    {
      "origin": [0,0,0],
      "size": [1,1,1]
    }
  ]
}
```

YAML:
```yaml
id: mynamespace:example_chest
item_resource:
  models:
    default: mynamespace:custom/furniture/chests/example_chest
properties:
  rotate: true
  rotate_smooth: true
behaviour:
  animation:
    model: my_filament_namespace:example_chest
  container:
    name: Example Chest
    size: 36
    purge: false
    open_animation: open
    close_animation: close
blocks:
  - origin: [0, 0, 0]
    size: [1, 1, 1]
```

The `animation` behaviour gets used for animations by various behaviours such as the `container` behaviour.

The blockbench model gets loaded from a filament datapack that contains the referenced model using the provided namespace

The `lock` behaviour also supports an animation using the `animation` behaviour

### Beach umbrella with custom size:
```json
{
  "id": "mynamespace:beach_umbrella_top",
  "itemResource": {
    "models": {
      "default": "mynamespace:custom/furniture/umbrella/beach_umbrella_top"
    }
  },
  "vanillaItem": "minecraft:leather_horse_armor",
  "properties": {
    "rotate": true,
    "rotateSmooth": true
  },
  "size": [3, -0.5]
}
```

The `size` field creates an interaction entity with a custom size instead of the default 1x1 sized one when no `blocks` are set. 


### Pile of gold ingots with solid 1x1 solid collision:
```json
{
  "id": "mynamespace:pile_of_gold_ingots",
  "itemResource": {
    "models": {
        "default": "mynamespace:custom/deco/misc/pile_of_gold_ingots"
    }
  },
  "properties": {
    "rotate": true,
    "rotateSmooth": true
  },
  "blocks": [
    {
      "origin": [0,0,0],
      "size": [1,1,1]
    }
  ]
}
```

