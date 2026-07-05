package net.zeronexus.quickstackcraft.fabric.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.zeronexus.quickstackcraft.logic.RuntimeTargets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists the in-game-editable whitelist/blacklist to {@code config/quickstackcraft-targets.json}
 * (same file name/format as the NeoForge version). Loaded once on startup; saved on each edit.
 */
public final class RuntimeTargetsStore {

    private static final Logger LOGGER = LoggerFactory.getLogger("QuickStackCraft");

    private RuntimeTargetsStore() {}

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("quickstackcraft-targets.json");
    }

    public static void init() {
        load();
        RuntimeTargets.setSaveCallback(RuntimeTargetsStore::save);
    }

    private static void load() {
        Path path = file();
        if (!Files.exists(path)) return;
        try {
            JsonObject root = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
            RuntimeTargets.load(readArray(root, "whitelist"), readArray(root, "blacklist"));
        } catch (Exception e) {
            LOGGER.warn("[QuickStackCraft] Could not read {}: {}", path.getFileName(), e.toString());
        }
    }

    private static synchronized void save() {
        JsonObject root = new JsonObject();
        root.add("whitelist", writeArray(RuntimeTargets.whitelistView()));
        root.add("blacklist", writeArray(RuntimeTargets.blacklistView()));
        try {
            Files.writeString(file(), root.toString());
        } catch (IOException e) {
            LOGGER.warn("[QuickStackCraft] Could not write quickstackcraft-targets.json: {}", e.toString());
        }
    }

    private static List<ResourceLocation> readArray(JsonObject root, String key) {
        List<ResourceLocation> out = new ArrayList<>();
        if (root.has(key) && root.get(key).isJsonArray()) {
            for (var el : root.getAsJsonArray(key)) {
                ResourceLocation id = ResourceLocation.tryParse(el.getAsString());
                if (id != null) out.add(id);
            }
        }
        return out;
    }

    private static JsonArray writeArray(Iterable<ResourceLocation> ids) {
        JsonArray arr = new JsonArray();
        for (ResourceLocation id : ids) arr.add(id.toString());
        return arr;
    }
}
