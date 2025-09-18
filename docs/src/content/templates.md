# Templates

Filament supports **templates**, which let you copy properties from one config into another.  
This reduces duplication and makes large packs easier to maintain.

---

## Basic Usage

First, define a base template, this can be done by either json/yaml in `filament/templates/` or by setting the `is_template` property.

```yaml
id: mynamespace:template_item
is_template: true # this is not needed if the file resides in `filament/templates/`
displayName: "<item_id_capitalized>" # this is a placeholder for the title-case verison the item id's path. For `mynamespace:christmas_tree` this would result in "Chistmas Tree"
vanillaItem: minecraft:diamond
item_resource:
  model:
    default: "mynamespace:item/<item_id>"
components:
  max_durability: 500
```

```yaml
id: mynamespace:template_item2
is_template: true # this is not needed if the file resides in `filament/templates/`
properties:
  lore: ["Namespace: <item_namespace>"]
```

Configurations with `is_template` set to `true` will only be added as template, not as item/block/etc. 

Those templates can then be used in your item/block/decoration/entity configuration like this:
```yaml
id: mynamespace:custom_item
template: mynamespace:template_item
components:
  dyed_color: 16711935
```

Multiple templates can be specified like this:

```yaml
id: mynamespace:custom_item2
templates:
  - mynamespace:template_item
  - mynamespace:template_item2
components:
  dyed_color: 6711935
```


Available placeholders are:
- `<item_id>`: For the path of an item id. For `mynamespace:christmas_tree` this would result in `christmas_tree`
- `<item_id_capitalized>`: Title-Case version the item-id's path. For `mynamespace:christmas_tree` this would result in `Chistmas Tree`
- `<item_namespace>`: Namespace of the item-id. For `mynamespace:christmas_tree` this would result in `mynamespace`
