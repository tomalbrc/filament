# Workstation

This system allows you to define custom crafting stations with configurable slot layouts, fuel handling, processing
times, and multiple recipe types: shaped, shapeless (single or multiple groups), and positional.

---

## Workstation Definition

Place JSON files in `data/<namespace>/filament/workstation/`.

### Required & Optional Fields

**`id`** (required)  
Unique identifier, e.g. `"mynamespace:herb_mixer"`.

**`menu_type`** (required)  
The GUI type. Possible values: `"minecraft:generic_9x6"`, `"minecraft:generic_9x5"`, `"minecraft:generic_9x5"`,
`"minecraft:generic_9x4"`, `"minecraft:generic_9x3"`, `"minecraft:generic_9x2"`, `"minecraft:generic_9x1"`,
`"minecraft:furnace"`,
`"minecraft:blast_furnace"`, `"minecraft:smoker"`, `"minecraft:generic_3x3"`.

**`grid`** (required for shaped recipes)  
Defines the logical grid dimensions for pattern matching. Example: `{ "rows": 3, "columns": 3 }`.

**`slots`** (required)  
List of slot definitions (see below).

**`background_item`** (optional)  
Item displayed in empty slots (e.g. invisible background).

Example:
`{ "id": "minecraft:paper", "components": { "item_model": "minecraft:air" } }`.

**`decorations`** (optional)  
Map of slot index -> decoration (clickable items). See Decoration definition.

**`processing_time`** (optional, default = `0`)

Smelting time for furnace-type recipes

**`persistent`** (optional, default = `false`)

- `false`: all items drop on ground when GUI closes.
- `true`: items stay inside the station inventory.

### Slot Definition

Each slot in the `slots` list has these fields:

**`name`** (required)  
Unique name for this slot (used in recipe JSON).

**`slot_index`** (required)  
Inventory slot number. For `generic_9x6`, valid indices are `0` to `53` (9 columns × 6 rows). You can use any numbers as
long as they don't conflict.

**`role`** (required)  
Possible values: `"input"` (player places items, consumed), `"output"` (results appear here, no placing), `"fuel"` (
accepts burnable items, vanilla fuel values).

**`group`** (optional, default = `"ingredients"`)  
Groups input slots together for multi‑group shapeless recipes. Example: `"group": "herbs"`.

**`row` and `col`** (optional, required for shaped recipes)  
Grid coordinates starting from 0. Example: `"row": 0, "col": 0`.

### Decoration Definition

For each entry in `decorations`, the key is the slot index, the value is an object with:

**`item`** (required)  
The item to display:
`{ "id": "...", "count": 1, "components": {...} }`.

**`command`** (optional)  
A command executed as the player when the decoration is clicked. Example: `"command": "say Hello!"`.

### Example: Herb Mixer (multiple groups)

```json
{
  "id": "mynamespace:herb_mixer",
  "menu_type": "minecraft:generic_9x6",
  "grid": {
    "rows": 2,
    "columns": 3
  },
  "slots": [
    {
      "name": "herb1",
      "slot_index": 1,
      "role": "input",
      "row": 0,
      "col": 0,
      "group": "herbs"
    },
    {
      "name": "herb2",
      "slot_index": 2,
      "role": "input",
      "row": 0,
      "col": 1,
      "group": "herbs"
    },
    {
      "name": "herb3",
      "slot_index": 10,
      "role": "input",
      "row": 1,
      "col": 0,
      "group": "herbs"
    },
    {
      "name": "herb4",
      "slot_index": 11,
      "role": "input",
      "row": 1,
      "col": 1,
      "group": "herbs"
    },
    {
      "name": "liquid1",
      "slot_index": 4,
      "role": "input",
      "row": 0,
      "col": 2,
      "group": "liquids"
    },
    {
      "name": "liquid2",
      "slot_index": 13,
      "role": "input",
      "row": 1,
      "col": 2,
      "group": "liquids"
    },
    {
      "name": "output",
      "slot_index": 16,
      "role": "output"
    }
  ],
  "processing_time": 0,
  "persistent": false
}
```

---

## Recipe Definition

Place files in `data/<namespace>/recipe/` (standard Minecraft recipe folder).  
`type` must be `"filament:station_recipe"`.

### Common Fields

| Field             | Type     | Description                                            |
|-------------------|----------|--------------------------------------------------------|
| `type`            | `string` | Always `"filament:station_recipe"`                     |
| `station`         | `string` | ID of the station definition                           |
| `processing_time` | `number` | Overrides station default (optional)                   |
| `permission`      | `string` | Permission the player needs in order to craft the item |

### Shapeless, Single Group

Uses the defined group default group `"ingredients"`.

```json
{
  "type": "filament:station_recipe",
  "station": "mynamespace:my_station",
  "ingredients": [
    "minecraft:oak_planks",
    "minecraft:oak_planks"
  ],
  "output": {
    "id": "minecraft:oak_slab",
    "count": 4
  }
}
```

### Shapeless, Multiple Groups

Each group field name must match a `group` defined on input slots. Value is an array of ingredients.

```json
{
  "type": "filament:station_recipe",
  "station": "mynamespace:herb_mixer",
  "herbs": [
    "minecraft:dandelion",
    "minecraft:poppy"
  ],
  "liquids": [
    "minecraft:water_bottle"
  ],
  "output": {
    "id": "minecraft:potion",
    "components": {
      "potion_contents": {
        "potion": "minecraft:healing"
      }
    }
  }
}
```

### Shaped (Pattern + Key)

Requires `grid` in station definition. Pattern uses characters; spaces are ignored.

```json
{
  "type": "filament:station_recipe",
  "station": "mynamespace:my_station",
  "pattern": [
    "###",
    "# #",
    "###"
  ],
  "key": {
    "#": "minecraft:oak_planks"
  },
  "output": {
    "id": "minecraft:chest",
    "count": 1
  }
}
```

Pattern dimensions cannot exceed station grid.

### Positional (Slot‑named)

Each input slot is directly named.

```json
{
  "type": "filament:station_recipe",
  "station": "mynamespace:my_station",
  "herb1": "minecraft:dandelion",
  "herb2": "minecraft:poppy",
  "liquid1": "minecraft:water_bottle",
  "output": {
    "id": "minecraft:rabbit_stew"
  }
}
```

### Fuel and Processing Time

If the station has `fuel` slots, timed recipes (processing_time > 0) will consume fuel automatically.  
Processing time can be set per recipe to override station default.

```json
{
  "type": "filament:station_recipe",
  "station": "mynamespace:furnace_station",
  "input": "minecraft:raw_iron",
  "fuel": "#minecraft:coals",
  "output": {
    "id": "minecraft:iron_ingot"
  },
  "processing_time": 200
}
```

- If the recipe doesn't define a fuel ingredient, it will use any vanilla fuel item for the recipe.

---

## Ingredient Formats

Wherever an ingredient is expected, you can use:

### String Item ID

```json
"minecraft:oak_planks"
```

### Tag (prefix `#`)

```json
"#minecraft:planks"
```

### Item with components

```json
{
  "id": "potion",
  "components": {
    "minecraft:potion_contents": {
      "potion": "minecraft:water"
    }
  }
}
```

---

## Output Format

Output is an object with at least `id`, optionally `count` and `components`.

```json
{
  "id": "minecraft:diamond",
  "count": 2
}
```

With components:

```json
{
  "id": "minecraft:potion",
  "components": {
    "potion_contents": {
      "potion": "minecraft:invisibility"
    }
  }
}
```

---

## GUI and Player Interaction

- **Input slots**: player can place items.
- **Fuel slots**: accept burnable items (vanilla fuel values).
- **Output slot**: for output, cant place items in.
- **Decorations**: slot with an item and optional command; click runs command as player.
- **Processing time**: for furnace‑like menus / workstations

If `persistent: false`, all items drop when the GUI closes.

---

## Example: Herb Mixer with Healing Potion

### Station: `mynamespace:herb_mixer.json`

```json
{
  "id": "mynamespace:herb_mixer",
  "menu_type": "minecraft:generic_9x6",
  "grid": {
    "rows": 2,
    "columns": 3
  },
  "slots": [
    {
      "name": "herb1",
      "slot_index": 1,
      "role": "input",
      "row": 0,
      "col": 0,
      "group": "herbs"
    },
    {
      "name": "herb2",
      "slot_index": 2,
      "role": "input",
      "row": 0,
      "col": 1,
      "group": "herbs"
    },
    {
      "name": "herb3",
      "slot_index": 10,
      "role": "input",
      "row": 1,
      "col": 0,
      "group": "herbs"
    },
    {
      "name": "herb4",
      "slot_index": 11,
      "role": "input",
      "row": 1,
      "col": 1,
      "group": "herbs"
    },
    {
      "name": "liquid1",
      "slot_index": 4,
      "role": "input",
      "row": 0,
      "col": 2,
      "group": "liquids"
    },
    {
      "name": "liquid2",
      "slot_index": 13,
      "role": "input",
      "row": 1,
      "col": 2,
      "group": "liquids"
    },
    {
      "name": "output",
      "slot_index": 16,
      "role": "output"
    }
  ],
  "processing_time": 0,
  "persistent": false
}
```

### Recipe: `mynamespace:healing_potion.json`

```json
{
  "type": "filament:station_recipe",
  "station": "mynamespace:herb_mixer",
  "herbs": [
    "minecraft:dandelion",
    "minecraft:poppy"
  ],
  "liquids": [
    "minecraft:water_bottle"
  ],
  "output": {
    "id": "minecraft:potion",
    "components": {
      "potion_contents": {
        "potion": "minecraft:healing"
      }
    }
  }
}
```

Now place a dandelion and a poppy in the four herb slots and a water bottle in either liquid slot, the output slot will
show a healing potion.
