## Creating a simple item

A minimal custom item definition in Filament looks like this:

### JSON

```json
{
  "id": "mynamespace:ruby",
  "vanillaItem": "minecraft:paper",
  "itemResource": {
    "parent": "minecraft:item/generated",
    "textures": {
      "layer0": "mynamespace:item/ruby"
    }
  },
  "properties": {
    "stackSize": 64
  }
}
```

### YAML

```yaml
id: mynamespace:ruby
vanillaItem: minecraft:paper

itemResource:
  parent: minecraft:item/generated
  textures:
    layer0: mynamespace:item/ruby

properties:
  stackSize: 64
```

Place the file at:

### JSON

```text
data/mynamespace/filament/item/ruby.json
```

### YAML

```text
data/mynamespace/filament/item/ruby.yml
```

Your datapack structure should look like this:

```text
MyDatapack/
├── pack.mcmeta
├── data/
│   └── mynamespace/
│       └── filament/
│           └── item/
│               └── ruby.json
└── assets/
    └── mynamespace/
        └── textures/
            └── item/
                └── ruby.png
```

## Automatically generated model

Instead of creating a separate model JSON manually, Filament can generate the item model automatically through the `itemResource` section.

The following fields:

### JSON

```json
{
  "itemResource": {
    "parent": "minecraft:item/generated",
    "textures": {
      "layer0": "mynamespace:item/ruby"
    }
  }
}
```

### YAML

```yaml
itemResource:
  parent: minecraft:item/generated
  textures:
    layer0: mynamespace:item/ruby
```

generate a model equivalent to:

```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "mynamespace:item/ruby"
  }
}
```

This means you only need to provide the texture file:

```text
assets/mynamespace/textures/item/ruby.png
```

## Giving the item

Reload the datapack and give yourself the item with:

```mcfunction
/give @p mynamespace:ruby
```

## References

- [Filament item examples](../content/item/item-examples)

- Example datapack:
  https://github.com/tomalbrc/filament/tree/main/example_datapack