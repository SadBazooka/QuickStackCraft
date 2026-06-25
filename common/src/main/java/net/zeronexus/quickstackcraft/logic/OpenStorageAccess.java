package net.zeronexus.quickstackcraft.logic;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

import java.util.List;

/**
 * Resolves the "storage content" slots of the container the player currently has open, when
 * that container is a supported external storage (e.g. a Sophisticated Backpack).
 *
 * <p>Used by the backpack Quick Stack / Dump buttons: the open backpack's contents are the
 * source, nearby world containers are the target.
 */
public final class OpenStorageAccess {

    private OpenStorageAccess() {}

    /**
     * @return the open container's storage slots (excluding player inventory and upgrade/special
     *         slots), or an empty list if the open container isn't a supported storage.
     */
    @ExpectPlatform
    public static List<Slot> getOpenStorageSlots(Player player) {
        throw new AssertionError("Not implemented");
    }
}
