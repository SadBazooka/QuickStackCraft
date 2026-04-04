# QuickStack & Craft

[![Modrinth](https://img.shields.io/modrinth/dt/quickstack-craft?logo=modrinth&label=Modrinth)](https://modrinth.com/mod/quickstack-craft)
[![GitHub](https://img.shields.io/github/v/release/shikyo13/QuickStackCraft?logo=github)](https://github.com/shikyo13/QuickStackCraft/releases)
[![License](https://img.shields.io/github/license/shikyo13/QuickStackCraft)](LICENSE)

All-in-one inventory QoL mod for Minecraft. Quick stack, dump all, slot favorites, and craft from nearby containers. Inspired by Terraria's inventory management.

Supports **Fabric** and **NeoForge** via [Architectury](https://modrinth.com/mod/architectury-api).

## Features

**Quick Stack to Nearby** - Deposit items into nearby containers that already hold matching items. Prioritizes fullest containers first.

**Dump All** - Deposit all non-favorited inventory items to any nearby container with space.

**Slot Favorites** - Alt+click any inventory slot to protect it from quick stack and dump. Persists through death.

**Craft from Nearby (JEI)** - With JEI installed, the [+] button checks both your inventory and nearby containers for ingredients. Click to pull ingredients from nearby storage into the crafting grid.

**Container Highlights** - Containers that received items glow with a gold outline and particle trail for 3 seconds.

**Modded Container Support** - Works with Sophisticated Storage, Iron Chests, and any mod exposing IItemHandler capabilities.

## Supported Versions

| Minecraft | Fabric | NeoForge |
|-|-|-|
| 1.21.1 | Yes | Yes |
| 1.21.4 | Yes | Yes |

## Installation

**Required on both client and server.**

### Dependencies
- [Architectury API](https://modrinth.com/mod/architectury-api) (required)
- [Fabric API](https://modrinth.com/mod/fabric-api) (Fabric only)
- [JEI](https://modrinth.com/mod/jei) (optional - enables craft-from-nearby)

### Multiplayer
Install on both the server and all clients. The server handles all item movement logic. Clients without the mod can still connect but won't have access to the features.

## Keybinds

| Action | Default |
|-|-|
| Quick Stack | Unbound (configure in Controls) |
| Dump All | Unbound (configure in Controls) |
| Favorite Toggle | Alt + Left Click |

## Building

```bash
git clone https://github.com/shikyo13/QuickStackCraft.git
cd QuickStackCraft
git checkout mc/1.21.1  # or mc/1.21.4
./gradlew build
```

Output jars are in `fabric/build/libs/` and `neoforge/build/libs/`.

## Links

- [Modrinth](https://modrinth.com/mod/quickstack-craft)
- [Website](https://zeronexus.net/mods.php)
- [Issues](https://github.com/shikyo13/QuickStackCraft/issues)
- [Buy Me a Coffee](https://buymeacoffee.com/zerotheabsolute)

## License

[MIT](LICENSE)
