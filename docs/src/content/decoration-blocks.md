# Blocks

The `blocks` field allows to specify where barriers blocks should be placed for the decoration, instead of client-side interaction entities.
It also allows for gaps:
~~~admonish example
```json5
{
  "blocks": [
    {
      "origin": [0.0, 0.0, 0.0],
      "size": [1.0, 1.0, 1.0]
    },
    {
      "origin": [2.0, 2.0, 2.0],
      "size": [1.0, 1.0, 1.0]
    }
    // Add more block config objects as needed
  ]
}
```
~~~

2 block high block configuration:
~~~admonish example
```json
{
  "blocks": [
    {
      "origin": [0.0, 0.0, 0.0],
      "size": [1.0, 2.0, 1.0]
    }
  ]
}
```
~~~

You can specify which block is used when specifying blocks with the `blocks` field using the `block` field. The default is `minecraft:barrier`
~~~admonish example
```json
{
  "block": "minecraft:chain[axis=y]",
  "blocks": [
    {
      "origin": [0.0, 0.0, 0.0],
      "size": [0.0, 2.0, 0.0]
    }
  ]
}
```
~~~
