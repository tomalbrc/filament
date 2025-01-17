# Using filament

Requires [fabric api](https://modrinth.com/mod/fabric-api) and [polymer](https://modrinth.com/mod/polymer) by patbox

filament mainly loads additional content using datapacks;

It also loads any **assets** from the `assets` folder in datapacks, if present.

The datapack structure should look something like this:
```
data/
|-- <namespace>/
|   |-- filament/
|   |   |-- item/
|   |   |   |-- <item_config>.json
|   |   |   |-- <item_config>.yaml
|   |   |
|   |   |-- block/
|   |   |   |-- <block_config>.json
|   |   |   |-- <block_config>.yaml
|   |   |
|   |   |-- decoration/
|   |   |   |-- <decoration_configs>.json
|   |   |
|   |   |-- model/
|   |       |-- <blockbench_model>.bbmodel
|   |       |-- <animated-java_model>.ajmodel
|
assets/
|   | ...
pack.mcmeta
```

So a configuration file path for a new block could look like this:
`MyDatapack/data/<namespace>/filament/block/myblock.json`

The files can also be located in subfolders for better organization:
`MyDatapack/data/<namespace>/filament/block/stone_like/myblock.json`

Filament supports yaml files as of 0.12.0.

Blockbench and Animated-Java models for decorations are supported using the [blockbench import library](https://github.com/tomalbrc/blockbench-import-library)

In versions 1.21.4 and higher, filament will automatically generate the item asset json under `namespace/items/itemname.json`.
You can provide your own item asset models using the `itemModel` field of an item (in the root structure of an item config) or by providing an item_model components using the `components` field.

---

## Common fields

Item-, block- and decoration configuration files share some common fields.

> **Note**: All fields support either camelCase or snake_case!

Those are:

- `id`: Identifier of the item/block/decoration.
- `vanillaItem`: The client-side item to use. This will be used for custom model data values in <= 1.21.1 
- `blockResource`: Here you can specify models for different blocks-states or just a single model for simple blocks.
- `itemResource`: Specifies the model of the item. Some item-behaviours may require you to provide models for their "states" (i.e. the `trap` behaviour) 
- `properties`: Properties differ for each config type, but all share the [item properties](content/item-properties.md).

## Behaviours 

Custom content is driven by something called `behaviours` in filament. 

Item behaviours give **items**, **block-items**, **blocks** and **decorations as well as **decoration-items** special capabilities, not normally possible using just vanilla item components.

This means that items, blocks and decorations are all capable of all item behaviours.

Block behaviours are not compatible with decorations and vice versa.

## Genuinely modded content!

On the server-side of things, all your custom items and blocks are genuinely modded, meaning they have their own id, instead of 'just' being vanilla items with components, only obtainable via recipes for example.

You can for example just use `/give @s mynamespace:myitem` instead of having to provide a dozen components or a datapack function.

For clients that don't have any mods or polymer installed, the item show up as the `vanillaItem` in the config for your content, but with all components correctly applied with your custom models.

In 1.21.1, custom model data override predicates will be automatically generated. You can also set the vanilla custom model data components in the `components` field of a config to use your own values.
This is not necessary in version 1.21.2 and above.
For 1.21.4, the new item model will be automatically generated. You can set the `itemModel` to prevent that.