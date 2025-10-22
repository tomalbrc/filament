# Item Groups

You can add custom item-groups for your items/blocks and decorations.
Those are accessible using `/polymer creative` or, when polymer is installed client-side, in the vanilla creative menu.

This can be done by adding an `item-groups.json`/`item-groups.yml` file in the root of the `filament` directory of your datapack.

Here is an example of an `item-groups.json`/`item-groups.yml`:
~~~admonish example
<!-- langtabs-start -->
```json
[
  {
    "id": "mynamespace:mygroup",
    "item": "mynamespace:myitem"
  },
  {
    "id": "mynamespace:myblockgroup",
    "item": "mynamespace:myitem",
    "literal": "<c:red>My Block Group"
  }
]
```

```yml
- id: mynamespace:mygroup
  item: mynamespace:myitem

- id: mynamespace:myblockgroup
  item: mynamespace:myitem
  literal: "<c:red>My Block Group"
```
<!-- langtabs-end -->

~~~

## Fields

### `id`

The identifier of your item group. This is used in the individual item/block/decoration files in the field `group`.
Each config. type supports the `group` field.

The id is also used for the translation key, see below.

### `item`

The id of the item shown as the Creative Tab Icon.

### `literal`

The name displayed for the item group - supports Placeholder API's basic text formatting. By default, a translatable string is used for resource-packs, using the `id` of the item-group.


The translation key looks like this: `mynamespace.itemGroup.mygroup` for an `id` of `mynamespace:mygroup`.

So an `en_us.json` for a resource-pack might look like this:
~~~admonish example
```json
{
  "mynamespace.itemGroup.mygroup": "My Group"
}
```
~~~

or for german (`de_de.json`):

~~~admonish example
```json
{
  "mynamespace.itemGroup.mygroup": "Meine Gruppe"
}
```
~~~
