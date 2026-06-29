package net.zeronexus.quickstackcraft.logic;

import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * In-game-editable whitelist / blacklist of block ids, layered on top of the config-file lists
 * in {@link net.zeronexus.quickstackcraft.config.QscConfig}. Edited via the "toggle target"
 * keybind and the {@code /quickstackcraft} commands.
 *
 * <p>State is held here (loader-independent); persistence (loading/saving a small JSON file) is
 * wired in by the platform via {@link #setSaveCallback}. This keeps {@link StorageFilter} able to
 * read the lists without depending on any platform API.
 */
public final class RuntimeTargets {

    private static final Set<ResourceLocation> whitelist = new CopyOnWriteArraySet<>();
    private static final Set<ResourceLocation> blacklist = new CopyOnWriteArraySet<>();

    private static volatile Runnable saveCallback = () -> {};

    private RuntimeTargets() {}

    public static boolean isWhitelisted(ResourceLocation id) {
        return whitelist.contains(id);
    }

    public static boolean isBlacklisted(ResourceLocation id) {
        return blacklist.contains(id);
    }

    /** @return true if it was added (false if already present) */
    public static boolean addWhitelist(ResourceLocation id) {
        blacklist.remove(id);
        boolean added = whitelist.add(id);
        if (added) save();
        return added;
    }

    /** @return true if it was removed */
    public static boolean removeWhitelist(ResourceLocation id) {
        boolean removed = whitelist.remove(id);
        if (removed) save();
        return removed;
    }

    /** @return true if it was added (false if already present) */
    public static boolean addBlacklist(ResourceLocation id) {
        whitelist.remove(id);
        boolean added = blacklist.add(id);
        if (added) save();
        return added;
    }

    /** @return true if it was removed */
    public static boolean removeBlacklist(ResourceLocation id) {
        boolean removed = blacklist.remove(id);
        if (removed) save();
        return removed;
    }

    public static void clearWhitelist() {
        if (!whitelist.isEmpty()) { whitelist.clear(); save(); }
    }

    public static void clearBlacklist() {
        if (!blacklist.isEmpty()) { blacklist.clear(); save(); }
    }

    public static Set<ResourceLocation> whitelistView() {
        return new TreeSet<>(whitelist);
    }

    public static Set<ResourceLocation> blacklistView() {
        return new TreeSet<>(blacklist);
    }

    /** Replace the in-memory lists (used when loading from disk). Does not trigger a save. */
    public static void load(Collection<ResourceLocation> wl, Collection<ResourceLocation> bl) {
        whitelist.clear();
        whitelist.addAll(wl);
        blacklist.clear();
        blacklist.addAll(bl);
    }

    /** Set by the platform to persist edits to disk. */
    public static void setSaveCallback(Runnable callback) {
        saveCallback = callback != null ? callback : () -> {};
    }

    private static void save() {
        saveCallback.run();
    }
}
