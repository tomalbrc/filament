# Blocks

The `blocks` field allows to use different blocks as solid hitboxes instead of ckient-side interaction entities and multiple blocks/multiblock decorations.

`block` currently doesnt have any effect, barrier blocks will always be used!

It also allows for gaps:
```json
{
  "blocks": [
    {
      "origin": [0.0, 0.0, 0.0],
      "size": [1.0, 1.0, 1.0],
      "block": "minecraft:barrier"
    },
    {
      "origin": [2.0, 2.0, 2.0],
      "size": [1.0, 1.0, 1.0],
      "block": "minecraft:barrier"
    },
    // Add more block config objects as needed
  ]
}
```

2 block high block configuration:
```json
{
  "blocks": [
    {
      "origin": [0.0, 0.0, 0.0],
      "size": [1.0, 2.0, 1.0],
      "block": "minecraft:barrier"
    }
  ]
}
```
