# QuickStack & Craft — 1.21.1 Fork

Terraria-style inventory management for Minecraft: quick-stack items into nearby
containers, dump your inventory in one click, protect favorited slots, and craft using
items pulled straight from nearby storage.

This is a **fork** of [QuickStack & Craft](https://www.curseforge.com/minecraft/mc-mods/quickstack-craft)
by **ZeroTheAbsolute**. It began as a 1.21.1 NeoForge compatibility fix and has grown into a
full fork with new features, safety fixes, and mod integrations.

> Maintained by **SadBazooka**. Original by **ZeroTheAbsolute**
> ([CurseForge](https://www.curseforge.com/minecraft/mc-mods/quickstack-craft) ·
> [upstream source](https://github.com/shikyo13/QuickStackCraft)).

## Features

- **Quick Stack to Nearby** — deposit items into nearby containers that already hold matching
  items (fullest containers first).
- **Dump All** — deposit all non-favorited inventory items into nearby storage with space.
- **Slot Favorites** — Alt+click a slot to protect it from quick stack / dump. Works (highlight and
  toggle) in your inventory **and any container screen** — chests, backpacks, etc. Persists through death.
- **Craft from Nearby (JEI)** — the `[+]` button checks your inventory **and** nearby containers
  for ingredients. Matches **any acceptable variant** of a recipe, so it fires immediately instead
  of waiting for JEI to cycle to an item you happen to own.
- **Sophisticated Backpacks integration** — Quick Stack / Dump buttons on the backpack screen that
  move the **backpack's** contents into nearby storage.
- **Storage-safe targeting** — only deposits into real storage (chests, barrels, shulkers, large
  modded inventories). Machines, generators, and furnaces are excluded so items can't be consumed
  or lost.
- **ItemLocks compatibility** — respects slots locked with the
  [ItemLocks](https://www.curseforge.com/minecraft/mc-mods/itemlocks) mod, so locked items are never
  quick-stacked or dumped. ItemLocks is the shared lock layer, so this also lines up with
  [ClientSort](https://www.curseforge.com/minecraft/mc-mods/clientsort). Soft dependency (reflection).
- **Container highlights** — containers that received items glow with a gold outline + particle trail.

## What's new in this fork

- Fixes the mixin crash that stopped the original from loading on Minecraft 1.21.1 (NeoForge):
  the `InventoryScreen` mixin now extends `AbstractContainerScreen` so the inherited `isHovering`
  resolves correctly.
- **Storage-only safety filter + config** — see `config/quickstackcraft-common.toml`
  (targeting mode, minimum slot count, per-block blacklist/whitelist). No more items vanishing
  into machine input slots.
- **JEI craft-from-nearby** reworked to consider every acceptable item per slot and place
  whichever variant you actually own.
- **Sophisticated Backpacks** stack/dump support (buttons on the backpack screen), on both loaders.
- **ItemLocks compatibility** and favorites that work in every container screen (not just the
  player inventory).
- **Fabric support** with full feature parity (including modded storage via the Fabric Transfer API),
  plus an optional Cloth Config / Mod Menu config screen.

## Configuration

Config file (generated on first launch): `config/quickstackcraft-common.toml` on **NeoForge**,
`config/quickstackcraft-fabric.json` on **Fabric**. On Fabric, if **Cloth Config** and **Mod Menu**
are installed you also get an in-game config screen (Mod Menu → QuickStack & Craft → Config).

| Option | Default | Description |
|---|---|---|
| `storageMode` | `STORAGE_ONLY` | `STORAGE_ONLY` (chests/barrels/shulkers/large inventories) or `ALL_CONTAINERS` (legacy, includes machines) |
| `minSlots` | `9` | Minimum slot count for an unrecognised inventory to count as storage |
| `blacklist` | furnaces, smoker, brewing stand | Blocks that are never valid targets (by id) |
| `whitelist` | *(empty)* | Blocks that are always valid targets (by id), overriding everything |

## Supported versions

| Minecraft | NeoForge | Fabric |
|-|-|-|
| 1.21.1 | Yes | Yes |

Both loaders have full feature parity, including the Sophisticated Backpacks integration (using the
official mod on NeoForge, and the community Fabric port on Fabric). Download the jar matching your
loader (`…-neoforge-…` or `…-fabric-…`).

## Dependencies

- [Architectury API](https://modrinth.com/mod/architectury-api) — required
- [JEI](https://modrinth.com/mod/jei) — optional, enables craft-from-nearby
- [Sophisticated Backpacks](https://www.curseforge.com/minecraft/mc-mods/sophisticated-backpacks) — optional, enables backpack buttons
- [ItemLocks](https://www.curseforge.com/minecraft/mc-mods/itemlocks) — optional, locked slots are skipped by quick stack / dump
- [ClientSort](https://www.curseforge.com/minecraft/mc-mods/clientsort) — optional; plays nice via ItemLocks (the shared lock layer)
- **Fabric only:** [Cloth Config](https://modrinth.com/mod/cloth-config) + [Mod Menu](https://modrinth.com/mod/modmenu) — optional, adds an in-game config screen

## Keybinds

| Action | Default |
|-|-|
| Quick Stack | Unbound (configure in Controls) |
| Dump All | Unbound (configure in Controls) |
| Favorite Toggle | Alt + Left Click |

## Building

```bash
./gradlew :neoforge:build
```

The NeoForge Sophisticated Backpacks integration is compiled against the SB and
Sophisticated Core jars. These are **not redistributed in this repo**; to build that module,
download the matching versions from CurseForge and drop them in `neoforge/libs/`:

- `sophisticatedbackpacks-1.21.1-<version>.jar`
- `sophisticatedcore-1.21.1-<version>.jar`

(update the file names referenced in `neoforge/build.gradle` if they differ).

## Credits & License

- Original **QuickStack & Craft** by **ZeroTheAbsolute** — https://www.curseforge.com/minecraft/mc-mods/quickstack-craft
- This fork maintained by **SadBazooka**.

Licensed under [GPL-3.0](LICENSE), the same license as the original mod. If you distribute
binaries, you must make the corresponding source available under the same terms.
