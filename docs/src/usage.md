# Using filament

Requires [fabric api](https://modrinth.com/mod/fabric-api) and [polymer](https://modrinth.com/mod/polymer) by patbox

filament mainly loads additional content using datapacks; 
It also loads any assets from the assets folder in datapacks, if present.

The datapack structure should look something like this:
```
data/
|-- <namespace>/
|   |-- filament/
|   |   |-- item/
|   |   |   |-- <item_config>.json
|   |
|   |   |-- block/
|   |   |   |-- <block_config>.json
|   |
|   |   |-- decoration/
|   |   |   |-- <decoration_configs>.json
|   |
|   |   |-- model/
|   |       |-- <blockbench_model>.bbmodel
|   |       |-- <animated-java_model>.ajmodel
|
pack.mcmeta
```

So a configuration file path for a new block could look like this:
`MyDatapack/data/<namespace>/filament/block/myblock.json`

The files can also be located in subfolders for better organization:
`MyDatapack/data/<namespace>/filament/block/stone_like/myblock.json`

Blockbench and Animated-Java models for decorations are supported using the [blockbench import library](https://github.com/tomalbrc/blockbench-import-library)

---

## Common fields

Item-, block- and decoration configuration files share some common fields.

Those are:

- `id`: Identifier of the item/block/decoration.
- `displayName`: An object that lets you specify translations for your item directly. This generates a lang file in the `filament` namespace.
  - Example: `"&6displayName": {"en_us": "Hello World", "de_de": "&6Hallo Welt"}`
- `blockResource`: Here you can specify models for different blocks-states or just a single model for simple blocks.
- `itemResource`: Specifies the model of the item. Some item-behaviours may require you to provide models for their "states" (i.e. the `trap` behaviour) 
- `properties`: Properties differ for each config type, but all share the [item properties](content/item-properties.md).

## Behaviours 

Custom content is driven by something called `behaviours` in filament. 

Item behaviours give simple items, block-items and decoration items special capabilities, not normally possible using just vanilla item components.

This means that items, blocks and decorations are all capable of all item behaviours.

Block behaviours are not compatible with decorations and vice versa.