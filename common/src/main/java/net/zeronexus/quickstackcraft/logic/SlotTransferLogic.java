package net.zeronexus.quickstackcraft.logic;

import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.zeronexus.quickstackcraft.util.ContainerAccess;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Quick stack / dump where the SOURCE is an arbitrary set of menu slots (e.g. the storage
 * slots of an open Sophisticated Backpack) rather than the player's main inventory.
 *
 * <p>Mirrors {@link QuickStackLogic} / {@link DumpLogic} but reads/writes through {@link Slot}
 * objects so it works with any inventory exposed by an open container.
 */
public final class SlotTransferLogic {

    private SlotTransferLogic() {}

    /**
     * @param sourceSlots slots to pull items out of
     * @param containers  nearby target containers (already distance-sorted and storage-filtered)
     * @param dumpAll     true = deposit everything (Dump All); false = only into containers that
     *                    already hold a matching item (Quick Stack)
     */
    public static TransferResult execute(List<Slot> sourceSlots, List<ContainerAccess> containers, boolean dumpAll) {
        int totalMoved = 0;
        Set<ContainerAccess> usedContainers = new HashSet<>();

        for (Slot slot : sourceSlots) {
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) continue;

            // Pick target containers for this item.
            List<ContainerAccess> targets;
            if (dumpAll) {
                targets = containers;
            } else {
                targets = containers.stream()
                        .filter(c -> c.containsItem(stack))
                        .sorted(Comparator.comparingInt((ContainerAccess c) -> c.countItem(stack)).reversed())
                        .toList();
            }
            if (targets.isEmpty()) continue;

            int before = stack.getCount();
            ItemStack remaining = stack.copy();
            for (ContainerAccess target : targets) {
                int rem0 = remaining.getCount();
                remaining = target.insertItem(remaining);
                if (remaining.getCount() < rem0) {
                    usedContainers.add(target);
                }
                if (remaining.isEmpty()) break;
            }

            int moved = before - remaining.getCount();
            if (moved > 0) {
                slot.remove(moved); // extract from the backing inventory (e.g. the backpack)
                totalMoved += moved;
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
