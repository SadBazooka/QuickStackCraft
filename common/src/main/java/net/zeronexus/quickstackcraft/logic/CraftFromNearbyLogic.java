package net.zeronexus.quickstackcraft.logic;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.zeronexus.quickstackcraft.util.ContainerAccess;

import java.util.List;

/**
 * Pulls crafting ingredients from player inventory and nearby containers
 * into the player's crafting grid.
 */
public final class CraftFromNearbyLogic {

    private CraftFromNearbyLogic() {}

    /**
     * @param player the player
     * @param ingredients 9 ItemStacks representing what each crafting slot needs
     * @param craftSlots the crafting grid slots (from InventoryMenu or CraftingMenu)
     * @param containers nearby containers to search
     * @return number of ingredients successfully placed
     */
    public static CraftResult execute(Player player, List<ItemStack> ingredients,
                                      List<Slot> craftSlots, List<ContainerAccess> containers) {
        Inventory playerInv = player.getInventory();
        int placed = 0;
        int containersUsed = 0;
        boolean usedContainer = false;

        for (int i = 0; i < Math.min(ingredients.size(), craftSlots.size()); i++) {
            ItemStack wanted = ingredients.get(i);
            if (wanted.isEmpty()) continue;

            Slot craftSlot = craftSlots.get(i);

            // Clear existing item in the craft slot back to inventory first
            if (!craftSlot.getItem().isEmpty()) {
                ItemStack existing = craftSlot.remove(craftSlot.getItem().getCount());
                playerInv.add(existing);
            }

            // Try player inventory first
            ItemStack found = extractOne(playerInv, wanted);

            // Then try nearby containers
            if (found.isEmpty()) {
                for (ContainerAccess ca : containers) {
                    found = extractOne(ca.container(), wanted);
                    if (!found.isEmpty()) {
                        usedContainer = true;
                        ca.container().setChanged();
                        break;
                    }
                }
            }

            if (!found.isEmpty()) {
                craftSlot.set(found);
                placed++;
            }
        }

        return new CraftResult(placed, ingredients.stream().filter(s -> !s.isEmpty()).count(),
                usedContainer);
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
