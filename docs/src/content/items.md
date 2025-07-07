# Items

## File Location

Item configuration files should be placed in the following directory:
```
MyDatapack/data/<namespace>/filament/item/myitem.json
```

## Example

Here is a basic example of an item configuration:

~~~admonish example
<!-- langtabs-start -->
```yaml
id: mynamespace:clown_horn
vanillaItem: minecraft:paper

# optional and only available in >= 1.21.4
itemModel: minecraft:mymodel

itemResource:
  models:
    default: mynamespace:custom/misc/clown_horn

properties:
  # your properties here, like stackSize, durability and more!

behaviour:
  # your behaviours here

components:
  # your components here
```

```json5
{
  "id": "mynamespace:clown_horn",
  "vanillaItem": "minecraft:paper",
  "itemModel": "minecraft:mymodel", // optional and only available in >= 1.21.4
  "itemResource": {
    "models": {
      "default": "mynamespace:custom/misc/clown_horn"
    }
  },
  "properties": {
    // your properties here, like stackSize, durability and more!
  },
  "behaviour": {
    // your behaviours here
  },
  "components": {
    // your components here
  }
}
```
<!-- langtabs-end -->

~~~

Item configurations have two required fields: `id` and `vanillaItem`.

---

## `id`

Your custom ID in the format `namespace:item_name`

---

## `vanillaItem`

The vanilla item to "overwrite". Filament (through Polymer) will create `custom_model_data` IDs for the generated resource pack. Your custom item will not inherit any other properties server-side from the vanilla item other than appearance. For interaction purposes on the client, it is important to choose the appropriate vanilla item to use here.

---

# Optional Fields

The fields `itemModel`, `itemResource`, `properties`, `group`, and `behaviour` are optional.

## `itemModel`

Path to an item model definition (in `assets/namespace/items/<name>.json`)
This overwrites the itemResource field.

---

## `itemResource`

Specifies the resource(s) for the item model. Depending on the item's behaviour(s), it may use additional keys/fields in `itemResource`.

~~~admonish info "Configurable Fields"
  - `models`: An object containing model definitions.
    - `default`: The default model for the item.
    - Additional keys may be required depending on the item's behaviour (e.g., `trapped` for a trap behaviour or `pulling_0`, `pulling_1`, `pulling_2` for bows).
  - `parent`: Parent of the item model if generated from textures
  - `textures`: An object containing texture definitions.
    - `default`: The textures for the default model of the item.
      - `layer0`: 
    - Additional keys may be required depending on the item's behaviour.
~~~

Example for automatic model generation based on textures:
~~~admonish example
<!-- langtabs-start -->
```yaml
item_resource:
  parent: item/generated
  textures:
    default:
      layer0: item/traps/allay_bottle
    trapped:
      layer0: item/traps/allay_bottle_trapped
```

```json
{
  "parent": "item/generated",
  "textures": {
    "default": {
      "layer0": "item/traps/allay_bottle"
    },
    "trapped": {
      "layer0": "item/traps/allay_bottle_trapped"
    }
  }
}
```
<!-- langtabs-end -->

~~~

This automatically creates:
- The item asset in `assets/<namespace>/items/<item-id>`
- Item model in `assets/<namespace>/models/item/<item-id>_default`
- Item model in `assets/<namespace>/models/item/<item-id>_trapped`

The same method works for bows or crossbow models, for example - just make sure to use the correct parent for the rotation in hand, etc.

---

## `properties`

Defines various properties for the item. The structure and contents of this field will depend on the specific properties being set.

---

## `group`

Defines the item-group for this item. See [Item Groups](item-groups.md) for more information.

---

## `behaviour`

Defines specific behaviours or interactions for the item. The structure and contents of this field will depend on the specific behaviours being set.

See [Item Behaviours](item-behaviours.md) for more information.

---

## `components`

Defines a set of vanilla minecraft components like `minecraft:food`, `minecraft:tool`, etc. 

The format is the same that is used in datapacks. This is only supported in minecraft version 1.20.5 and later

