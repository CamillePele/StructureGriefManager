# Structure Grief Manager (SGM) ![Version](https://img.shields.io/badge/Version-1.20.1-blue) ![Forge](https://img.shields.io/badge/Forge-1.20.1-orange)

A server-side administration mod to protect structures while allowing temporary interactions and block restoration.

SGM allows players to explore and interact with protected zones (like Villages or Custom Dungeons) without permanently damaging them. Blocks can be broken but disappear and respawn later, or placed blocks can decay automatically.

## Features

### Zone Protection
Automatically detects structures or custom defined regions.
- **Structure Detection:** Protects all Minecraft structures (Villages, Fortresses, Bastions, etc.) out of the box.
- **Custom Zones:** Define specific areas using coordinates in the config.

### Advanced Filtering
Precise control over which structures are protected using Regex whitelist/blacklist.
- Example: Protect only `minecraft:.*village.*` or exclude `minecraft:end_city`.

### Lifecycle System
Define how blocks inside zones behave with configurable Rules:
- **`DENY`**: Completely prevents breaking or placing blocks.
- **`ALLOW_RESPAWN`**: Players can break blocks, but they **respawn** after a configurable time (default 30s).
    - *Preserves TileEntity NBT data (Chests contents are restored!)*.
- **`ALLOW_DECAY`**: Players can place blocks (e.g., scaffolding, bridges), but they **decay** (disappear) after a configurable time.

### Physics Engine
Advanced dependency handling ensures realistic and safe restoration.
- **Gravity & Support:** When a supporting block respawns or breaks, dependent blocks (Torches, Banners, Doors, Carpets) are handled correctly to prevent item drops or floating blocks.
- **Smart Restoration:** Blocks respawn in the correct order to maintain structural integrity.

### Safety & Grief Prevention
- **Explosion Protection:** Creepers and TNT cannot permanently destroy protected structures (blocks respawn).
- **Environment control:** Blocks decayed or respawned do not drop items, preventing infinite resource farming.

## Configuration
The configuration is located in `config/sgm.json`.
It supports **JSON Schema**, offering autocompletion in generic code editors (VSCode, IntelliJ).

**Example Configuration:**
```json
{
  "settings": {
    "tick_interval": 20,
    "respawn_time": 600,
    "decay_time": 200,
    "debug_mode": false
  },
  "zones": [
    {
      "name": "Global Village Protection",
      "type": "STRUCTURE",
      "priority": 10,
      "structure_whitelist": [
        "minecraft:.*village.*"
      ],
      "structure_blacklist": [
        "minecraft:village_desert"
      ],
      "rules": {
        "break": [
          {
            "targets": [
              "#c:chests"
            ],
            "action": "DENY"
          },
          {
            "targets": [
              "*"
            ],
            "action": "ALLOW_RESPAWN"
          }
        ],
        "place": [
          {
            "targets": [
              "*"
            ],
            "action": "ALLOW_DECAY",
            "timer": 60
          }
        ]
      }
    }
  ]
}
```

## Commands
Commands require OP Level 2.

- `/sgm info`: Displays the active Zone and protection rules at your current position.
- `/sgm reload`: Reloads the configuration file from disk.
- `/sgm memory`: (Debug) Displays current active block checks in the chunk controller.

## Installation
1. Download the `.jar` file.
2. Drop it into your server's `/mods` folder.
3. (Optional) Install on client for visual sync (better tooltips/feedback), but the mod works **Server-Side Only**.

---
*Created for Minecraft 1.20.1 Forge.*
