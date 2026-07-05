package net.zeronexus.quickstackcraft.logic.fabric;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

import java.util.List;

/**
 * Fabric implementation of {@link net.zeronexus.quickstackcraft.logic.OpenStorageAccess}.
 *
 * <p>Supports the Fabric port of Sophisticated Backpacks/Core as a soft dependency: all references
 * to its classes live in {@link SbStorageSlots}, only touched (and classloaded) when the mod is
 * actually present.
 */
public final class OpenStorageAccessImpl {

    private static final boolean SOPHISTICATED_CORE_LOADED =
            FabricLoader.getInstance().isModLoaded("sophisticatedcore");

    private OpenStorageAccessImpl() {}

    public static List<Slot> getOpenStorageSlots(Player player) {
        if (!SOPHISTICATED_CORE_LOADED) {
            return List.of();
        }
        return SbStorageSlots.collect(player);
    }
}
