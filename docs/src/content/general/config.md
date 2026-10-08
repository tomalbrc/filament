# Configuration

Configuration files live in `config/filament/`. Two files are used:

- `config/filament/general.json` -> core mod settings
- `config/filament/web-editor.json` -> the web editor server

On first boot after upgrading from an older version, the legacy files (`config/filament.json` and `config/filament-editor.json`) are automatically migrated into the new folder and then removed. Existing values are preserved; new options are filled in with their defaults.

## General (`config/filament/general.json`)

### `debug`

Prints additional debug information

---

### `use_minimessage`

Uses minimessage instead of fabrics Placeholder API for formatted text.

### `commands`

Enable `/filament` and its sub-commands

---

### `prevent_adventure_mode_decoration_interaction`

Prevent interaction with decorations for players in adventure mode.

---

### `alternative_block_placement`

Oraxen/ItemsAdder compatible decoration placement (decoration placement rotated by 180°)

---

### `alternative_cosmetic_placement`

Oraxen/ItemsAdder compatible cosmetic placement (adds armorstand offset)

---

### `add_custom_menu_assets`

Adds custom menu assets to the generated resource pack.

---

### `resourcepack_required`

Whether the resource pack is required for clients to join. When `true`, clients without the pack are rejected.

---

### `decoration_placement_previews`

Enables the placement preview mode for decorations. When enabled, players can sneak + press **F** while holding a decoration item to toggle a ghost preview of the decoration where it would be placed.

---

### `preview_glow`

Applies a glow effect to the placement preview elements. Defaults to `true`.

---

### `preview_glow_color`

The glow color for placement preview elements, as a hex RGB integer (e.g. `0x00FF00` for green). Defaults to `0x00FF00`.

---

### `preview_transparency`

The opacity multiplier applied to preview textures. `0.5` means 50% opacity. Defaults to `0.5`.

---

## Web Editor (`config/filament/web-editor.json`)

### `enabled`

Enables the web editor server.

---

### `passwordLogin`

Requires a username and password to access the web editor.

---

### `defaultUser`

The default username for the web editor login. Only used when `passwordLogin` is `true`.

---

### `defaultPassword`

The default password for the web editor login. Only used when `passwordLogin` is `true`.

---

### `bindIp`

The IP address the web editor server binds to. Defaults to `0.0.0.0` (all interfaces).

---

### `bindPort`

The port the web editor server listens on. Defaults to `25599`.

---

### `externalAddress`

The address advertised to clients for connecting to the web editor. Defaults to `http://127.0.0.1:25599`.
