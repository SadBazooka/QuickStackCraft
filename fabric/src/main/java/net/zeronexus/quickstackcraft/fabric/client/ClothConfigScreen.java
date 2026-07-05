package net.zeronexus.quickstackcraft.fabric.client;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.zeronexus.quickstackcraft.config.QscConfig;
import net.zeronexus.quickstackcraft.fabric.config.FabricConfig;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Builds an in-game config screen with Cloth Config (shown via Mod Menu). Edits the same
 * {@link QscConfig} values as the JSON file and saves them back through {@link FabricConfig}.
 *
 * <p>Only referenced when Cloth Config is present (see {@code ModMenuIntegration}).
 */
public final class ClothConfigScreen {

    private ClothConfigScreen() {}

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("QuickStack & Craft"))
                .setSavingRunnable(FabricConfig::save);

        ConfigEntryBuilder eb = builder.entryBuilder();

        ConfigCategory targeting = builder.getOrCreateCategory(Component.literal("Targeting"));
        targeting.addEntry(eb.startEnumSelector(Component.literal("Storage mode"),
                        QscConfig.StorageMode.class, QscConfig.storageMode)
                .setDefaultValue(QscConfig.StorageMode.STORAGE_ONLY)
                .setSaveConsumer(v -> QscConfig.storageMode = v)
                .build());
        targeting.addEntry(eb.startBooleanToggle(Component.literal("Allow unrecognized by slot count"),
                        QscConfig.allowUnrecognizedBySlotCount)
                .setDefaultValue(false)
                .setSaveConsumer(v -> QscConfig.allowUnrecognizedBySlotCount = v)
                .build());
        targeting.addEntry(eb.startIntField(Component.literal("Min slots"), QscConfig.minSlots)
                .setDefaultValue(9).setMin(1).setMax(100_000)
                .setSaveConsumer(v -> QscConfig.minSlots = v)
                .build());
        targeting.addEntry(eb.startIntField(Component.literal("Scan radius"), QscConfig.radius)
                .setDefaultValue(8).setMin(1).setMax(64)
                .setSaveConsumer(v -> QscConfig.radius = v)
                .build());
        targeting.addEntry(eb.startStrList(Component.literal("Blacklist (block ids)"), idsToList(QscConfig.blacklist))
                .setSaveConsumer(list -> QscConfig.blacklist = listToIds(list))
                .build());
        targeting.addEntry(eb.startStrList(Component.literal("Whitelist (block ids)"), idsToList(QscConfig.whitelist))
                .setSaveConsumer(list -> QscConfig.whitelist = listToIds(list))
                .build());

        ConfigCategory highlight = builder.getOrCreateCategory(Component.literal("Highlight"));
        highlight.addEntry(eb.startStrField(Component.literal("Color (RRGGBB hex)"), QscConfig.highlightColorHex)
                .setDefaultValue("FFD700")
                .setSaveConsumer(v -> QscConfig.highlightColorHex = v)
                .build());
        highlight.addEntry(eb.startDoubleField(Component.literal("Alpha (0.0-1.0)"), QscConfig.highlightAlpha)
                .setDefaultValue(0.8).setMin(0.0).setMax(1.0)
                .setSaveConsumer(v -> QscConfig.highlightAlpha = v)
                .build());
        highlight.addEntry(eb.startIntField(Component.literal("Duration (ms)"), QscConfig.highlightDurationMs)
                .setDefaultValue(3000).setMin(100).setMax(60_000)
                .setSaveConsumer(v -> QscConfig.highlightDurationMs = v)
                .build());

        return builder.build();
    }

    private static List<String> idsToList(Set<ResourceLocation> ids) {
        List<String> out = new ArrayList<>();
        for (ResourceLocation id : ids) out.add(id.toString());
        return out;
    }

    private static Set<ResourceLocation> listToIds(List<String> list) {
        Set<ResourceLocation> ids = new HashSet<>();
        for (String s : list) {
            ResourceLocation id = ResourceLocation.tryParse(s.trim());
            if (id != null) ids.add(id);
        }
        return ids;
    }
}
