# Using filament

Requires [fabric api](https://modrinth.com/mod/fabric-api) and [polymer](https://modrinth.com/mod/polymer) by patbox

filament loads additional content using datapacks.

The datapack file structure should look something like this:
```
data/
|-- <namespace>/
|   |-- filament/
|   |   |-- item/
|   |   |   |-- <item_advancement_file>.json
|   |
|   |   |-- block/
|   |   |   |-- <block_advancement_file>.json
|   |
|   |   |-- decoration/
|   |   |   |-- <decoration_configs>.json
|   |
|   |   |-- ajmodel/
|   |       |-- <decoration_advancement_file>.json
|
pack.mcmeta
```

So a configuration file path for a new block could look like this:
`MyDatapack/data/<namespace>/filament/block/myblock.json`

The files can also be located in subfolders for better organization:
`MyDatapack/data/<namespace>/filament/block/stone_like/myblock.json`

Animated-Java models for decorations are supported using Animated-Java's JSON exporter and the [resin](https://github.com/tomalbrc/resin) library
