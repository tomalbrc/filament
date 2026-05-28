## Creating a simple block

Filament blocks are defined in `data/<namespace>/filament/block/` using a configuration file.

A block definition combines:
- a unique `id`
- a `blockModelType`
- resource mappings for models
- optional `properties` for hardness etc.
- optional `behaviours`

---

## Minimal block

### JSON

```json
{
  "id": "mynamespace:ruby_block",
  "blockModelType": "full_block",
  "blockResource": {
    "models": {
      "default": "mynamespace:block/ruby_block"
    }
  }
}
```

### YAML

```yaml
id: mynamespace:ruby_block
blockModelType: full_block,
blockResource:
  models:
    default: mynamespace:block/ruby_block
```

---

## Behaviours and block states

Block states in Filament are introduced only through explicit `behaviours`.

If no behaviour is defined:
- the block has no additional state keys
- only the `default` model is used

---

## Example with a behaviour

### JSON

```json
{
  "id": "mynamespace:lamp_block",
  "blockModelType": "full_block",
  "behaviours": {
    "powerlevel": {}
  },
  "blockResource": {
    "models": {
      "powerlevel=0": "mynamespace:block/lamp_off",
      "powerlevel=1": "mynamespace:block/lamp_on"
    }
  }
}
```

---

### YAML

```yaml
id: mynamespace:lamp_block
blockModelType: full_block,
behaviours:
  powerlevel: {}
blockResource:
  models:
    powerlevel=0: mynamespace:block/lamp_off
    powerlevel=1: mynamespace:block/lamp_on
```

---

## Summary

- `id` identifies the block in Filament
- `behaviours` define block-states sources and functionality
- `blockResource` maps resulting states to models
- `blockModelType` client-side block shape and collision, see [Block Model Types](../content/block/block-model-types.md)
- `properties` define destroyTime and other properties
