# Behaviours

There are currently only 2 block specific behaviours

Example of a normal plant block with the behaviours set:
```json
{
  "id": "mynamespace:myblock",
  "blockResource": {
    "models": {
      "default": "mynamespace:custom/block/myblock"
    }
  },
  "blockModelType": "biome_plant_block",
  "properties": {
    "destroyTime": 0,
    "blockBase": "minecraft:stone",
    "itemBase": "minecraft:paper"
  },
  "behaviour": {
    "powersource": {
      "value": 15
    },
    "repeater": {
      "delay": 0,
      "loss": 0
    }
  }
}
```


### `powersource`:

- `value`: The redstone power value the block is emitting

### `repeater`:

- `delay`: Delay in ticks.
- `loss`: Power loss during "transfer".
