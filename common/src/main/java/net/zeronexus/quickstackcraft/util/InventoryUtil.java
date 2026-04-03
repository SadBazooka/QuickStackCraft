package net.zeronexus.quickstackcraft.util;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.function.IntPredicate;

/**
 * Helpers for iterating player inventory slots and comparing stacks.
 */
public final class InventoryUtil {

    // Player inventory slot ranges
    public static final int HOTBAR_START = 0;
    public static final int HOTBAR_END = 9;        // exclusive
    public static final int MAIN_INV_START = 9;
    public static final int MAIN_INV_END = 36;      // exclusive
    public static final int ARMOR_START = 36;
    public static final int ARMOR_END = 40;          // exclusive
    public static final int OFFHAND_SLOT = 40;

    private InventoryUtil() {}

    /**
     * Iterate over eligible player inventory slots, skipping armor, offhand,
     * and optionally hotbar. Calls the consumer for each eligible slot index.
     * Returns the number of slots processed.
     */
    public static void forEachEligibleSlot(Inventory inventory, boolean skipHotbar, IntPredicate slotConsumer) {
        int start = skipHotbar ? MAIN_INV_START : HOTBAR_START;
        for (int i = start; i < MAIN_INV_END; i++) {
            if (!slotConsumer.test(i)) break;
        }
    }
}
