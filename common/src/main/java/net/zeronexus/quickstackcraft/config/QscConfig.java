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
 * stays loader-independent. If no platform populates these (e.g. a stripped Fabric build),
 * the defaults below are used, which keep the safe "real storage only" behaviour.
 */
public final class QscConfig {

    private QscConfig() {}

    /** How dump / quick-stack decides which nearby containers are valid targets. */
    public enum StorageMode {
        /** Only real storage: chests, barrels, shulkers, and large modded inventories. (default) */
        STORAGE_ONLY,
        /** Any block exposing an item inventory, including machines (the old behaviour). */
        ALL_CONTAINERS
    }

    public static volatile StorageMode storageMode = StorageMode.STORAGE_ONLY;

    /**
     * In STORAGE_ONLY mode, an otherwise-unrecognised container must have at least this many
     * slots to be considered storage. Filters out machines/generators with a handful of
     * input/output slots while still allowing large modded inventories.
     */
    public static volatile int minSlots = 9;

    /** Blocks that are NEVER valid targets, by id (e.g. "minecraft:furnace"). */
    public static volatile Set<ResourceLocation> blacklist = defaultBlacklist();

    /** Blocks that are ALWAYS valid targets, overriding every other rule (e.g. "mekanism:basic_bin"). */
    public static volatile Set<ResourceLocation> whitelist = new HashSet<>();

    private static Set<ResourceLocation> defaultBlacklist() {
        Set<ResourceLocation> set = new HashSet<>();
        // Vanilla "container-like but not storage" blocks whose item slots would consume items.
        set.add(ResourceLocation.withDefaultNamespace("furnace"));
        set.add(ResourceLocation.withDefaultNamespace("blast_furnace"));
        set.add(ResourceLocation.withDefaultNamespace("smoker"));
        set.add(ResourceLocation.withDefaultNamespace("brewing_stand"));
        return set;
    }
}
