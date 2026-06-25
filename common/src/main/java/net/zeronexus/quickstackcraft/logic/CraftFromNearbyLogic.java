package net.zeronexus.quickstackcraft.logic;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.zeronexus.quickstackcraft.util.ContainerAccess;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Pulls crafting ingredients from player inventory and nearby containers into the player's
 * crafting grid.
 *
 * <p>Each crafting slot may accept several variants (e.g. any plank). For each slot we place
 * whichever variant the player actually owns - checking the live player inventory first, then
 * nearby containers. Items are consumed as they're placed, so cross-slot contention is handled
 * naturally; we fill the most-constrained slots (fewest variants) first to maximise success.
 */
public final class CraftFromNearbyLogic {

    private CraftFromNearbyLogic() {}

    /**
     * @param player     the player
     * @param slotOptions per crafting slot, the list of acceptable item variants (empty = unused)
     * @param craftSlots the crafting grid slots (from InventoryMenu or CraftingMenu)
     * @param containers nearby containers to search
     * @return how many slots were filled
     */
    public static CraftResult execute(Player player, List<List<ItemStack>> slotOptions,
                                      List<Slot> craftSlots, List<ContainerAccess> containers) {
        Inventory playerInv = player.getInventory();

        // Return anything currently in the grid back to the inventory before refilling.
        for (Slot craftSlot : craftSlots) {
            if (!craftSlot.getItem().isEmpty()) {
                ItemStack existing = craftSlot.remove(craftSlot.getItem().getCount());
                playerInv.add(existing);
            }
        }

        int slotCount = Math.min(slotOptions.size(), craftSlots.size());

        // Fill most-constrained slots first (fewest acceptable variants).
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < slotCount; i++) {
            if (!slotOptions.get(i).isEmpty()) order.add(i);
        }
        order.sort(Comparator.comparingInt(i -> slotOptions.get(i).size()));

        int placed = 0;
        int needed = order.size();
        boolean usedContainer = false;

        for (int i : order) {
            List<ItemStack> variants = slotOptions.get(i);
            Slot craftSlot = craftSlots.get(i);

            ItemStack found = ItemStack.EMPTY;

            // Try player inventory first, across all acceptable variants.
            for (ItemStack variant : variants) {
                found = extractOne(playerInv, variant);
                if (!found.isEmpty()) break;
            }

            // Then nearby containers.
            if (found.isEmpty()) {
                outer:
                for (ItemStack variant : variants) {
                    for (ContainerAccess ca : containers) {
                        found = extractOne(ca.container(), variant);
                        if (!found.isEmpty()) {
                            usedContainer = true;
                            ca.container().setChanged();
                            break outer;
                        }
                    }
                }
            }

            if (!found.isEmpty()) {
                craftSlot.set(found);
                placed++;
            }
        }

        return new CraftResult(placed, needed, usedContainer);
    }

    /**
     * Extract one item matching the wanted stack from a container.
     * Returns the extracted single-item stack, or EMPTY if not found.
     */
    private static ItemStack extractOne(Container container, ItemStack wanted) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack slot = container.getItem(i);
            if (!slot.isEmpty() && ItemStack.isSameItemSameComponents(slot, wanted)) {
                return container.removeItem(i, 1);
            }
        }
        return ItemStack.EMPTY;
    }

    public record CraftResult(int placed, long needed, boolean usedContainers) {
        public boolean isComplete() {
            return placed == needed;
        }
    }
}
