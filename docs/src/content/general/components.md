# Components

# Backpack Component

The `filament:backpack` component can be used to turn any item with a container component into a "portable" container that can be opened by using the item in-hand.

The container size is limited to 27 slots at the moment.

### Fields:

- `size`: Size of the container
- `prevent_placement`: Prevents placement for block-items
- `title_prefix`: Allows for a custom background using fonts. Make sure to use negative spacing the width of the menu, in order for the normal container name to be in the correct position.

Example usage:
`/give @s minecraft:poppy[filament:backpack={size:27,prevent_placement:true},container=[]]`

As json:
```json
{
  "components": {
    "filament:backpack": {
      "size": 108,
      "prevent_placement": true,
      "title_prefix": "<font:customui:menus>A</font><font:customui:negativespace>X</font>"
    }
  }
}
```

# Skin Components

Filament comes with 2 components that any item to be skinned (visually) by any other item.

You can for example create a diamond pickaxe skin that can be applied to a wooden pickaxe or a diamond. The wooden pickaxe would look like a diamond pickaxe but function like a wooden pickaxe. You could also allow the diamond pickaxe skin to be applied to shovels too!

Filament cosmetics are also supported, which allows you to create cosmetic skins!

Player can simply drag a skin onto the item they want the skin applied to.

## Skin Component Format

The `filament:skin` component works similar to the `minecraft:repairable` component. You can either specify a list of items or an item-tag, for which item you want the skin to be applicable for.

```json
{
  "components": {
    "filament:skin": ["minecraft:wooden_pickaxe", "iron_pickaxe"]
  }
}
```

You can also use item-tags:
```json
{
  "components": {
    "filament:skin": "#minecraft:pickaxes"
  }
}
```
