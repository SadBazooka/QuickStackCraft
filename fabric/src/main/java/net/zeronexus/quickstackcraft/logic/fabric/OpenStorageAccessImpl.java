package net.zeronexus.quickstackcraft.logic.fabric;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

import java.util.List;

/**
 * Fabric implementation of {@link net.zeronexus.quickstackcraft.logic.OpenStorageAccess}.
 * The Sophisticated Backpacks integration is currently NeoForge-only, so this returns no slots.
 */
public final class OpenStorageAccessImpl {

    private OpenStorageAccessImpl() {}

    public static List<Slot> getOpenStorageSlots(Player player) {
        return List.of();
    }
}
