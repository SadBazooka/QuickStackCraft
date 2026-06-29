package net.zeronexus.quickstackcraft.neoforge.config;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.zeronexus.quickstackcraft.config.QscConfig;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * NeoForge config for QuickStack &amp; Craft (config/quickstackcraft-common.toml). Bakes resolved
 * values into the loader-agnostic {@link QscConfig} holder on load/reload.
 */
public final class NeoConfig {

    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.EnumValue<QscConfig.StorageMode> STORAGE_MODE;
    private static final ModConfigSpec.BooleanValue ALLOW_BY_SLOT_COUNT;
    private static final ModConfigSpec.IntValue MIN_SLOTS;
    private static final ModConfigSpec.IntValue RADIUS;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> BLACKLIST;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> WHITELIST;

    private static final ModConfigSpec.ConfigValue<String> HIGHLIGHT_COLOR;
    private static final ModConfigSpec.DoubleValue HIGHLIGHT_ALPHA;
    private static final ModConfigSpec.IntValue HIGHLIGHT_DURATION_MS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Controls where Quick Stack / Dump All are allowed to deposit items.").push("targeting");

        STORAGE_MODE = builder
                .comment(
                        "Which nearby blocks count as valid deposit targets.",
                        "STORAGE_ONLY   - only recognised storage: chests, barrels, shulkers, tagged storage",
                        "                 (incl. the quickstackcraft:storage tag), and your whitelist. Machines,",
                        "                 generators, furnaces and trash cans are rejected so items can't be lost. (default)",
                        "ALL_CONTAINERS - any block with an item inventory, including machines (use at your own risk).")
                .defineEnum("storageMode", QscConfig.StorageMode.STORAGE_ONLY);

        ALLOW_BY_SLOT_COUNT = builder
                .comment(
                        "In STORAGE_ONLY mode, also accept any UNRECOGNISED inventory with at least 'minSlots' slots.",
                        "OFF by default: many machines (e.g. Iron Furnaces) expose lots of slots and would be wrongly",
                        "treated as storage. Turn on only if you understand the risk.")
                .define("allowUnrecognizedBySlotCount", false);

        MIN_SLOTS = builder
                .comment("Slot threshold used only when allowUnrecognizedBySlotCount is enabled.")
                .defineInRange("minSlots", 9, 1, 100_000);

        RADIUS = builder
                .comment("Spherical scan radius in blocks for finding nearby containers (covers up/down too).")
                .defineInRange("radius", 8, 1, 64);

        BLACKLIST = builder
                .comment(
                        "Blocks that are NEVER valid targets, by id. Overrides everything except the whitelist.",
                        "Editable in-game with the keybind / /quickstackcraft commands, or by hand here.")
                .defineListAllowEmpty("blacklist",
                        List.of("minecraft:furnace", "minecraft:blast_furnace", "minecraft:smoker", "minecraft:brewing_stand",
                                "trashcans:item_trash_can", "trashcans:ultimate_trash_can"),
                        () -> "minecraft:furnace",
                        o -> o instanceof String s && ResourceLocation.tryParse(s) != null);

        WHITELIST = builder
                .comment(
                        "Blocks that are ALWAYS valid targets, by id. Overrides every other rule.",
                        "Use this to force-allow storage the filter doesn't recognise. Editable in-game with the",
                        "keybind / /quickstackcraft commands, or by hand here.")
                .defineListAllowEmpty("whitelist",
                        List.<String>of(),
                        () -> "modid:block",
                        o -> o instanceof String s && ResourceLocation.tryParse(s) != null);

        builder.pop();

        builder.comment("How highlighted containers are drawn (e.g. after a transfer, or via the preview keybind).")
                .push("highlight");

        HIGHLIGHT_COLOR = builder
                .comment("Outline colour as RRGGBB hex (no leading #). Default FFD700 (gold).")
                .define("color", "FFD700", o -> o instanceof String s && s.matches("(?i)[0-9a-f]{6}"));

        HIGHLIGHT_ALPHA = builder
                .comment("Outline opacity, 0.0 (invisible) to 1.0 (solid).")
                .defineInRange("alpha", 0.8, 0.0, 1.0);

        HIGHLIGHT_DURATION_MS = builder
                .comment("How long highlights stay on screen, in milliseconds.")
                .defineInRange("durationMs", 3000, 100, 60_000);

        builder.pop();
        SPEC = builder.build();
    }

    private NeoConfig() {}

    /** Copy the parsed config values into the shared {@link QscConfig} holder. */
    public static void bake() {
        QscConfig.storageMode = STORAGE_MODE.get();
        QscConfig.allowUnrecognizedBySlotCount = ALLOW_BY_SLOT_COUNT.get();
        QscConfig.minSlots = MIN_SLOTS.get();
        QscConfig.radius = RADIUS.get();
        QscConfig.blacklist = toIds(BLACKLIST.get());
        QscConfig.whitelist = toIds(WHITELIST.get());
        QscConfig.highlightColorHex = HIGHLIGHT_COLOR.get();
        QscConfig.highlightAlpha = HIGHLIGHT_ALPHA.get();
        QscConfig.highlightDurationMs = HIGHLIGHT_DURATION_MS.get();
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
