package net.zeronexus.quickstackcraft.logic.fabric;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageInventorySlot;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds all direct references to Sophisticated Core classes (Fabric port). Loaded only when the mod
 * is present (see {@link OpenStorageAccessImpl}).
 *
 * <p>{@link StorageInventorySlot} is the slot type used for a storage's actual contents; upgrade
 * slots, filter slots, and the player inventory are different types, so filtering on it cleanly
 * targets just the backpack's stored items.
 */
final class SbStorageSlots {

    private SbStorageSlots() {}

    static List<Slot> collect(Player player) {
        List<Slot> storageSlots = new ArrayList<>();
        for (Slot slot : player.containerMenu.slots) {
            if (slot instanceof StorageInventorySlot) {
                storageSlots.add(slot);
            }
        }
        return storageSlots;
    }
}
