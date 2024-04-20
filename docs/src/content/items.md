# Items

## File

Item configuration files are to be placed in `MyDatapack/data/<namespace>/filament/item/myitem.json` 

## Contents

Item configurations only have 2 required fields: `id` and `vanillaItem`
### `id`: 

Your custom id in the format "namespace:item_name", as it will show up in-game

### `vanillaItem`: 

The item to "overwrite". filament (through polymer) will create custom_model_data id's for the generated resource-pack. Your custom item will not inherit any other properties serverside from it other than appearance, if the `models` field is not set. For interaction purposes on client however, it is important to choose the right item to use here. 

---

The fields `models`, `properties` and `behaviour` are optional.

`models` only has a "default" field for the model, depending on the behaviour(s) of the item, it may use additional keys/fields in `models`.

The "trap" item behaviour for example requires a "trapped" model.

`models` is soon to be replaced with an `itemResource` field/structure to allow for model generation by only providing a texture

---

Basic example:
```json
{
  "id": "mynamespace:clown_horn",
  "vanillaItem": "minecraft:paper",
  "models": {
    "default": "mynamespace:custom/misc/clown_horn"
  },
  "properties": {
    ...
  },
  "behaviour": {
    ...
  }
}
```