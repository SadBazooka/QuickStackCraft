package net.zeronexus.quickstackcraft.fabric.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.zeronexus.quickstackcraft.config.QscConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * Fabric config: a plain JSON file ({@code config/quickstackcraft-fabric.json}) that mirrors the
 * NeoForge options and bakes them into {@link QscConfig}. Self-contained (Gson only) so it works
 * with no extra dependencies; the optional Cloth Config / Mod Menu screen edits these same values.
 */
public final class FabricConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger("QuickStackCraft");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private FabricConfig() {}

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("quickstackcraft-fabric.json");
    }

    /** Load the file into {@link QscConfig} (filling in any missing keys with defaults + saving). */
    public static void load() {
        Path path = file();
        if (Files.exists(path)) {
            try {
                readInto(JsonParser.parseString(Files.readString(path)).getAsJsonObject());
            } catch (Exception e) {
                LOGGER.warn("[QuickStackCraft] Could not read {}: {}", path.getFileName(), e.toString());
            }
        }
        save(); // (re)write so new keys/defaults appear in the file
    }

    private static void readInto(JsonObject o) {
        QscConfig.storageMode = parseMode(getString(o, "storageMode", QscConfig.storageMode.name()));
        QscConfig.allowUnrecognizedBySlotCount = getBool(o, "allowUnrecognizedBySlotCount", QscConfig.allowUnrecognizedBySlotCount);
        QscConfig.minSlots = getInt(o, "minSlots", QscConfig.minSlots);
        QscConfig.radius = clamp(getInt(o, "radius", QscConfig.radius), 1, 64);
        if (o.has("blacklist") && o.get("blacklist").isJsonArray()) QscConfig.blacklist = parseIds(o.getAsJsonArray("blacklist"));
        if (o.has("whitelist") && o.get("whitelist").isJsonArray()) QscConfig.whitelist = parseIds(o.getAsJsonArray("whitelist"));
        QscConfig.highlightColorHex = getString(o, "highlightColor", QscConfig.highlightColorHex);
        QscConfig.highlightAlpha = clamp(getDouble(o, "highlightAlpha", QscConfig.highlightAlpha), 0.0, 1.0);
        QscConfig.highlightDurationMs = clamp(getInt(o, "highlightDurationMs", QscConfig.highlightDurationMs), 100, 60_000);
    }

    /** Write current {@link QscConfig} values back to disk (also used by the config screen). */
    public static void save() {
        JsonObject o = new JsonObject();
        o.addProperty("storageMode", QscConfig.storageMode.name());
        o.addProperty("allowUnrecognizedBySlotCount", QscConfig.allowUnrecognizedBySlotCount);
        o.addProperty("minSlots", QscConfig.minSlots);
        o.addProperty("radius", QscConfig.radius);
        o.add("blacklist", idsToArray(QscConfig.blacklist));
        o.add("whitelist", idsToArray(QscConfig.whitelist));
        o.addProperty("highlightColor", QscConfig.highlightColorHex);
        o.addProperty("highlightAlpha", QscConfig.highlightAlpha);
        o.addProperty("highlightDurationMs", QscConfig.highlightDurationMs);
        try {
            Files.writeString(file(), GSON.toJson(o));
        } catch (IOException e) {
            LOGGER.warn("[QuickStackCraft] Could not write quickstackcraft-fabric.json: {}", e.toString());
        }
    }

    private static QscConfig.StorageMode parseMode(String s) {
        try {
            return QscConfig.StorageMode.valueOf(s);
        } catch (IllegalArgumentException e) {
            return QscConfig.StorageMode.STORAGE_ONLY;
        }
    }

    private static Set<ResourceLocation> parseIds(JsonArray arr) {
        Set<ResourceLocation> ids = new HashSet<>();
        for (JsonElement el : arr) {
            ResourceLocation id = ResourceLocation.tryParse(el.getAsString());
            if (id != null) ids.add(id);
        }
        return ids;
    }

    private static JsonArray idsToArray(Set<ResourceLocation> ids) {
        JsonArray arr = new JsonArray();
        for (ResourceLocation id : ids) arr.add(id.toString());
        return arr;
    }

    private static String getString(JsonObject o, String k, String def) { return o.has(k) ? o.get(k).getAsString() : def; }
    private static boolean getBool(JsonObject o, String k, boolean def) { return o.has(k) ? o.get(k).getAsBoolean() : def; }
    private static int getInt(JsonObject o, String k, int def) { return o.has(k) ? o.get(k).getAsInt() : def; }
    private static double getDouble(JsonObject o, String k, double def) { return o.has(k) ? o.get(k).getAsDouble() : def; }
    private static int clamp(int v, int lo, int hi) { return Math.max(lo, Math.min(hi, v)); }
    private static double clamp(double v, double lo, double hi) { return Math.max(lo, Math.min(hi, v)); }
}
