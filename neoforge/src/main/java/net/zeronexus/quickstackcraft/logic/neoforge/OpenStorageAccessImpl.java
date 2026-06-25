package net.zeronexus.quickstackcraft.logic.neoforge;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.neoforged.fml.ModList;

import java.util.List;

/**
 * NeoForge implementation of {@link net.zeronexus.quickstackcraft.logic.OpenStorageAccess}.
 *
 * <p>Sophisticated Backpacks/Core is a soft dependency: all references to its classes are
 * isolated in {@link SbStorageSlots}, which is only touched (and therefore classloaded) when
 * the mod is actually present. This keeps the mod working without Sophisticated Core installed.
 */
public final class OpenStorageAccessImpl {

    private static final boolean SOPHISTICATED_CORE_LOADED =
            ModList.get().isLoaded("sophisticatedcore");

    private OpenStorageAccessImpl() {}

    public static List<Slot> getOpenStorageSlots(Player player) {
        if (!SOPHISTICATED_CORE_LOADED) {
            return List.of();
        }
        return SbStorageSlots.collect(player);
    }
}
