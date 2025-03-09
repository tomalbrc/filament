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
