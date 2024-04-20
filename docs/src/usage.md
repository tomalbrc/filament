# Using filament

Requires [fabric api](https://modrinth.com/mod/fabric-api) and [polymer](https://modrinth.com/mod/polymer) by patbox

filament loads additional content using datapacks.

The datapack file structure should look something like this:
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
|   |       |-- <blockbench_model>.json
|
pack.mcmeta
```

So a configuration file path for a new block could look like this:
`MyDatapack/data/<namespace>/filament/block/myblock.json`

The files can also be located in subfolders for better organization:
`MyDatapack/data/<namespace>/filament/block/stone_like/myblock.json`

Blockbench and Animated-Java models for decorations are supported using the [blockbench import library](https://github.com/tomalbrc/blockbench-import-library)
