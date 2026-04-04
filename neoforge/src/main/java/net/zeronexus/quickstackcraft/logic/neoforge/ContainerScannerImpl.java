package net.zeronexus.quickstackcraft.logic.neoforge;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * NeoForge implementation: queries IItemHandler capability for modded containers
 * (Sophisticated Storage, Iron Chests, etc.) and wraps them as Container.
 */
public final class ContainerScannerImpl {

    private ContainerScannerImpl() {}

    public static Container getContainerFromCapability(Level level, BlockPos pos) {
        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
        if (handler != null && handler.getSlots() > 0) {
            return new ItemHandlerContainer(handler);
        }
        return null;
    }

    /**
     * Adapts IItemHandler to Container interface for use with ContainerAccess.
     */
    private static class ItemHandlerContainer implements Container {
        private final IItemHandler handler;

        ItemHandlerContainer(IItemHandler handler) {
            this.handler = handler;
        }

        @Override
        public int getContainerSize() {
            return handler.getSlots();
        }

        @Override
        public boolean isEmpty() {
            for (int i = 0; i < handler.getSlots(); i++) {
                if (!handler.getStackInSlot(i).isEmpty()) return false;
            }
            return true;
        }

        @Override
        public ItemStack getItem(int slot) {
            return handler.getStackInSlot(slot);
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            return handler.extractItem(slot, amount, false);
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return handler.extractItem(slot, handler.getStackInSlot(slot).getCount(), false);
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            // Extract existing, then insert new
            handler.extractItem(slot, handler.getStackInSlot(slot).getCount(), false);
            if (!stack.isEmpty()) {
                handler.insertItem(slot, stack, false);
            }
        }

        @Override
        public void setChanged() {
            // No-op: IItemHandler implementations handle their own dirty marking
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public void clearContent() {
            for (int i = 0; i < handler.getSlots(); i++) {
                handler.extractItem(i, handler.getStackInSlot(i).getCount(), false);
            }
        }
    }
}
