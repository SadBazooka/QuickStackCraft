package net.zeronexus.quickstackcraft.config;

import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

/**
 * Platform-agnostic holder for the mod's runtime configuration.
 *
 * <p>The actual config file is read on each platform (NeoForge uses {@code ModConfigSpec})
 * and the resolved values are baked into these static fields on load/reload. Common code
 * (e.g. {@link net.zeronexus.quickstackcraft.logic.StorageFilter}) reads from here so it
 * stays loader-independent. Defaults keep the safe "real storage only" behaviour.
 *
 * <p>The base black/whitelists below come from the config file. In-game edits (keybind /
 * commands) live in {@link net.zeronexus.quickstackcraft.logic.RuntimeTargets} and are merged
 * on top, so config edits and in-game edits coexist.
 */
public final class QscConfig {

    private QscConfig() {}

    /** How dump / quick-stack decides which nearby containers are valid targets. */
    public enum StorageMode {
        /** Only recognised storage: chests, barrels, shulkers, tagged storage, and your whitelist. (default) */
        STORAGE_ONLY,
        /** Any block exposing an item inventory, including machines (use at your own risk). */
        ALL_CONTAINERS
    }

    public static volatile StorageMode storageMode = StorageMode.STORAGE_ONLY;

    /**
     * In STORAGE_ONLY mode, also accept any unrecognised inventory with at least {@link #minSlots}
     * slots. OFF by default because many machines (e.g. Iron Furnaces) expose lots of slots and
     * would wrongly look like storage.
     */
    public static volatile boolean allowUnrecognizedBySlotCount = false;

    /** Slot threshold used only when {@link #allowUnrecognizedBySlotCount} is enabled. */
    public static volatile int minSlots = 9;

    /** Spherical scan radius (blocks) for finding nearby containers. Covers up/down too. */
    public static volatile int radius = 8;

    /** Base blacklist from the config file: blocks that are NEVER valid targets. */
    public static volatile Set<ResourceLocation> blacklist = defaultBlacklist();

    /** Base whitelist from the config file: blocks that are ALWAYS valid targets. */
    public static volatile Set<ResourceLocation> whitelist = new HashSet<>();

    // --- Highlight rendering (read client-side) ---

    /** Outline colour for highlighted containers, as "RRGGBB" hex. */
    public static volatile String highlightColorHex = "FFD700"; // gold

    /** Outline alpha 0.0-1.0. */
    public static volatile double highlightAlpha = 0.8;

    /** How long highlights stay on screen, in milliseconds. */
    public static volatile int highlightDurationMs = 3000;

    private static Set<ResourceLocation> defaultBlacklist() {
        Set<ResourceLocation> set = new HashSet<>();
        // Vanilla "container-like but not storage" blocks whose item slots would consume items.
        set.add(ResourceLocation.withDefaultNamespace("furnace"));
        set.add(ResourceLocation.withDefaultNamespace("blast_furnace"));
        set.add(ResourceLocation.withDefaultNamespace("smoker"));
        set.add(ResourceLocation.withDefaultNamespace("brewing_stand"));
        // Item voiders that must never receive items.
        set.add(ResourceLocation.fromNamespaceAndPath("trashcans", "item_trash_can"));
        set.add(ResourceLocation.fromNamespaceAndPath("trashcans", "ultimate_trash_can"));
        return set;
    }
}
