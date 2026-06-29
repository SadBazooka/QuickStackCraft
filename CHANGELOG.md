# Changelog

All notable changes to this fork are documented here. This fork targets **Minecraft 1.21.1 (NeoForge)**.

## v0.1.2

### Items no longer lost to machines
- Targeting is now a **strict allowlist** — only real storage receives items: chest/barrel/shulker
  tags (`c:chests`, `c:barrels`, `c:shulker_boxes`), vanilla storage types, the new
  `quickstackcraft:storage` tag, and your whitelist. Machines, generators, furnaces, and trash cans
  are excluded by default.
- Fixes Iron Furnaces (and any machine exposing many slots) eating items — the old "enough slots =
  storage" heuristic is now opt-in via `allowUnrecognizedBySlotCount` (default off).
- Shipped `quickstackcraft:storage` tag pre-covers Functional Storage drawers, Ender Storage, and
  DimStorage. Trash cans are hard-blacklisted.

### Easy whitelist/blacklist editing (no file editing needed)
- New keybind **Toggle Looked-at Block as Storage Target** — look at a block, press the key to
  add/remove it; chat confirms. Saved to `config/quickstackcraft-targets.json`.
- New commands `/quickstackcraft whitelist|blacklist add|remove|list|clear` (acts on the looked-at
  block, or a given id). Op-gated, since the lists are server-wide.

### See where loot goes
- New keybind **Highlight Storage Targets** — outlines every eligible container in range without
  transferring anything.

### More config
- Adjustable scan `radius` (spherical, covers up/down), replacing the hardcoded 8.
- Highlight `color`, `alpha`, and `durationMs` are now configurable.
- Quick Stack / Dump keybinds now also work in the world, not just inside a GUI.

> All keybinds are unbound by default — set them in Options → Controls → QuickStack & Craft.

## v0.1.1

First fork release for Minecraft 1.21.1 (NeoForge).

- Fixed the mixin crash that prevented the original from loading on 1.21.1 (the `InventoryScreen`
  mixin now extends `AbstractContainerScreen` so the inherited `isHovering` resolves).
- Storage-safe dump/stack targeting with a config file.
- JEI craft-from-nearby reworked to match any acceptable recipe variant you own, instead of only the
  variant JEI is currently displaying.
- Sophisticated Backpacks integration: Quick Stack / Dump buttons on the backpack screen.

## v0.1.0

Original release by ZeroTheAbsolute (upstream). See
<https://www.curseforge.com/minecraft/mc-mods/quickstack-craft>.
