# Web Editor

The **Filament Web Editor** lets you view and manage your custom content in. It provides a live overview of all registered assets with 3D previews

---

## Setup

### Configuration
To enable the editor, check the configuration file located at `config/filament-editor.json`.

* **Enabled:** Set `"enabled": true`
* **Networking:** By default, the editor binds to port `25599` on address `0.0.0.0`. This means it listens on all available network interfaces on the host machine.
* **External Access:** Set the `external_address` (e.g., `http://your-server-ip:25599`) for remote access. By default, this is set to `http://127.0.0.1:25599`.

### Accessing the Editor
To access the web interface, run the following command in-game:
> `/filament editor`

This command generates a unique session with a key for your Player UUID (or none if run from console):
1.  **The Login URL:** A link to the editor interface with a session id.
2.  **The Login Key:** A one-time 5-digit numeric key used to authenticate the browser session.

---

Changes made in the editor can be saved by clicking "Write to File" to apply the current file directly to the datapack.
