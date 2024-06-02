# Decoration Examples

### Animated chest with solid 1x1 solid collision:
```json
{
  "id": "mynamespace:example_chest",
  "model": "mynamespace:custom/furniture/chests/example_chest",
  "properties": {
    "rotate": true
  },
  "behaviour": {
    "animation": {
      "model": "myFilamentNamespace:example_chest"
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

The `animation` behaviour gets used for animations by the container behaviour.

It gets loaded from a filament datapack that contains the referenced model using the provided namespace

The `lock` behaviour also supports an animation using the `animation` behaviour

### Beach umbrella with custom size:
```json
{
  "id": "mynamespace:beach_umbrella_top",
  "model": "mynamespace:custom/furniture/umbrella/beach_umbrella_top",
  "itemModel": "mynamespace:custom/furniture/umbrella/beach_umbrella_top",
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
  "model": "mynamespace:custom/deco/misc/pile_of_gold_ingots",
  "itemModel": "mynamespace:custom/deco/misc/pile_of_gold_ingots",
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

