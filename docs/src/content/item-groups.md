# Item Groups

You can add custom item-groups for your items/blocks and decorations.

This can be done by adding an `item-groups.json` file in the root of the `filament` directory of your datapack.

Here is an example of an `item-groups.json` file:
~~~admonish example
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

So an `en_US.json` for a resrouce-pack might look like this:
~~~admonish example
```json
{
  "mynamespace.itemGroup.mygroup": "My Group"
}
```
~~~

or for german (`de_DE.json`):

~~~admonish example
```json
{
  "mynamespace.itemGroup.mygroup": "Meine Gruppe"
}
```
~~~
