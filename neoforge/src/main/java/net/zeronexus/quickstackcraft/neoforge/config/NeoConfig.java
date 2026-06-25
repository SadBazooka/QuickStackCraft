package net.zeronexus.quickstackcraft.neoforge.config;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.zeronexus.quickstackcraft.config.QscConfig;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * NeoForge server config for QuickStack &amp; Craft. Defines the file
 * (config/quickstackcraft-server.toml) and bakes resolved values into the
 * loader-agnostic {@link QscConfig} holder on load/reload.
 */
public final class NeoConfig {

    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.EnumValue<QscConfig.StorageMode> STORAGE_MODE;
    private static final ModConfigSpec.IntValue MIN_SLOTS;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> BLACKLIST;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> WHITELIST;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Controls where Quick Stack / Dump All are allowed to deposit items.").push("targeting");

        STORAGE_MODE = builder
                .comment(
                        "Which nearby blocks count as valid deposit targets.",
                        "STORAGE_ONLY  - only real storage: chests, barrels, shulkers, and large modded inventories.",
                        "                Machines/generators are excluded so items can't be consumed/lost. (default)",
                        "ALL_CONTAINERS - any block with an item inventory, including machines (use at your own risk).")
                .defineEnum("storageMode", QscConfig.StorageMode.STORAGE_ONLY);

        MIN_SLOTS = builder
                .comment(
                        "In STORAGE_ONLY mode, a container that isn't a recognised chest/barrel/shulker (by tag",
                        "or vanilla type) must have at least this many slots to count as storage. This lets large",
                        "modded inventories through while excluding machines that only expose a few input/output slots.")
                .defineInRange("minSlots", 9, 1, 100_000);

        BLACKLIST = builder
                .comment(
                        "Blocks that are NEVER valid targets, by id. Overrides everything except the whitelist.",
                        "Example: \"minecraft:furnace\", \"mekanism:metallurgic_infuser\"")
                .defineListAllowEmpty("blacklist",
                        List.of("minecraft:furnace", "minecraft:blast_furnace", "minecraft:smoker", "minecraft:brewing_stand"),
                        () -> "minecraft:furnace",
                        o -> o instanceof String s && ResourceLocation.tryParse(s) != null);

        WHITELIST = builder
                .comment(
                        "Blocks that are ALWAYS valid targets, by id. Overrides every other rule (including the",
                        "STORAGE_ONLY filter and the blacklist). Use this to force-allow a specific storage block",
                        "that the heuristics miss, e.g. \"mekanism:basic_bin\".")
                .defineListAllowEmpty("whitelist",
                        List.<String>of(),
                        () -> "modid:block",
                        o -> o instanceof String s && ResourceLocation.tryParse(s) != null);

        builder.pop();
        SPEC = builder.build();
    }

    private NeoConfig() {}

    /** Copy the parsed config values into the shared {@link QscConfig} holder. */
    public static void bake() {
        QscConfig.storageMode = STORAGE_MODE.get();
        QscConfig.minSlots = MIN_SLOTS.get();
        QscConfig.blacklist = toIds(BLACKLIST.get());
        QscConfig.whitelist = toIds(WHITELIST.get());
    }

    private static Set<ResourceLocation> toIds(List<? extends String> raw) {
        Set<ResourceLocation> ids = new HashSet<>();
        for (String s : raw) {
            ResourceLocation id = ResourceLocation.tryParse(s);
            if (id != null) ids.add(id);
        }
        return ids;
    }
}
