package net.zeronexus.quickstackcraft.logic;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.zeronexus.quickstackcraft.util.ContainerAccess;
import net.zeronexus.quickstackcraft.util.InventoryUtil;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.IntPredicate;

/**
 * Quick stack: deposit items into nearby containers that already hold at least one matching item.
 * Prioritizes containers by quantity of matching item (fullest first).
 */
public final class QuickStackLogic {

    private QuickStackLogic() {}

    public static TransferResult execute(Player player, List<ContainerAccess> containers,
                                         boolean skipHotbar, IntPredicate isFavorited) {
        Inventory playerInv = player.getInventory();
        int totalMoved = 0;
        Set<ContainerAccess> usedContainers = new HashSet<>();

        int start = skipHotbar ? InventoryUtil.MAIN_INV_START : InventoryUtil.HOTBAR_START;
        for (int slot = start; slot < InventoryUtil.MAIN_INV_END; slot++) {
            if (isFavorited.test(slot)) continue;

            ItemStack stack = playerInv.getItem(slot);
            if (stack.isEmpty()) continue;

            int beforeCount = stack.getCount();

            // Find containers already holding this item type, sorted by quantity (fullest first)
            List<ContainerAccess> targets = containers.stream()
                    .filter(c -> c.containsItem(stack))
                    .sorted(Comparator.comparingInt((ContainerAccess c) -> c.countItem(stack)).reversed())
                    .toList();

            ItemStack remaining = stack;
            for (ContainerAccess target : targets) {
                remaining = target.insertItem(remaining);
                usedContainers.add(target);
                if (remaining.isEmpty()) break;
            }

            int moved = beforeCount - remaining.getCount();
            totalMoved += moved;

            if (remaining.isEmpty()) {
                playerInv.setItem(slot, ItemStack.EMPTY);
            } else if (moved > 0) {
                playerInv.setItem(slot, remaining);
            }
        }

        List<BlockPos> blockPositions = new ArrayList<>();
        List<Integer> entityIds = new ArrayList<>();
        for (ContainerAccess ca : usedContainers) {
            ca.container().setChanged();
            if (ca.isBlockContainer()) {
                blockPositions.add(ca.blockPos());
            } else if (ca.entity() != null) {
                entityIds.add(ca.entity().getId());
            }
        }

        return new TransferResult(totalMoved, usedContainers.size(), blockPositions, entityIds);
    }
}
