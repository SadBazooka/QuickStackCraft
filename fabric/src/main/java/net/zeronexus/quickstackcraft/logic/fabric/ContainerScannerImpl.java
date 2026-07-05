package net.zeronexus.quickstackcraft.logic.fabric;

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.zeronexus.quickstackcraft.util.BulkInsertContainer;

/**
 * Fabric implementation: modded storage (Sophisticated Storage, drawers, etc.) exposes its items
 * through the Fabric Transfer API ({@code ItemStorage.SIDED}) rather than implementing vanilla
 * {@link Container}. This queries that API and adapts a slotted item {@link Storage} to a
 * {@link Container} so the rest of the mod can treat it like any other container.
 *
 * <p>Deposits go through the storage's own transactional {@code insert} (via
 * {@link BulkInsertContainer}) so capacity and multi-slot distribution are handled correctly with
 * no risk of losing items. Reads/extractions use the per-slot views.
 */
public final class ContainerScannerImpl {

    private ContainerScannerImpl() {}

    public static Container getContainerFromCapability(Level level, BlockPos pos) {
        Storage<ItemVariant> storage = ItemStorage.SIDED.find(level, pos, null);
        if (storage instanceof SlottedStorage<ItemVariant> slotted && slotted.getSlotCount() > 0) {
            return new SlottedStorageContainer(slotted);
        }
        return null;
    }

    /** Adapts a Fabric {@link SlottedStorage} of items to a vanilla {@link Container}. */
    private static final class SlottedStorageContainer implements Container, BulkInsertContainer {

        private final SlottedStorage<ItemVariant> storage;

        SlottedStorageContainer(SlottedStorage<ItemVariant> storage) {
            this.storage = storage;
        }

        @Override
        public ItemStack quickstackcraft$insertBulk(ItemStack stack) {
            if (stack.isEmpty()) return ItemStack.EMPTY;
            ItemVariant variant = ItemVariant.of(stack);
            try (Transaction tx = Transaction.openOuter()) {
                long inserted = storage.insert(variant, stack.getCount(), tx);
                tx.commit();
                int remaining = stack.getCount() - (int) Math.min(inserted, stack.getCount());
                return remaining <= 0 ? ItemStack.EMPTY : stack.copyWithCount(remaining);
            }
        }

        @Override
        public int getContainerSize() {
            return storage.getSlotCount();
        }

        @Override
        public boolean isEmpty() {
            for (int i = 0; i < storage.getSlotCount(); i++) {
                SingleSlotStorage<ItemVariant> s = storage.getSlot(i);
                if (!s.getResource().isBlank() && s.getAmount() > 0) return false;
            }
            return true;
        }

        @Override
        public ItemStack getItem(int slot) {
            if (slot < 0 || slot >= storage.getSlotCount()) return ItemStack.EMPTY;
            SingleSlotStorage<ItemVariant> s = storage.getSlot(slot);
            ItemVariant v = s.getResource();
            if (v.isBlank() || s.getAmount() <= 0) return ItemStack.EMPTY;
            return v.toStack((int) Math.min(s.getAmount(), Integer.MAX_VALUE));
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            if (slot < 0 || slot >= storage.getSlotCount() || amount <= 0) return ItemStack.EMPTY;
            SingleSlotStorage<ItemVariant> s = storage.getSlot(slot);
            ItemVariant v = s.getResource();
            if (v.isBlank()) return ItemStack.EMPTY;
            try (Transaction tx = Transaction.openOuter()) {
                long extracted = s.extract(v, amount, tx);
                tx.commit();
                return extracted <= 0 ? ItemStack.EMPTY : v.toStack((int) extracted);
            }
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            if (slot < 0 || slot >= storage.getSlotCount()) return ItemStack.EMPTY;
            return removeItem(slot, (int) Math.min(storage.getSlot(slot).getAmount(), Integer.MAX_VALUE));
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            if (slot < 0 || slot >= storage.getSlotCount()) return;
            SingleSlotStorage<ItemVariant> s = storage.getSlot(slot);
            try (Transaction tx = Transaction.openOuter()) {
                ItemVariant cur = s.getResource();
                if (!cur.isBlank() && s.getAmount() > 0) {
                    s.extract(cur, s.getAmount(), tx);
                }
                if (!stack.isEmpty()) {
                    s.insert(ItemVariant.of(stack), stack.getCount(), tx);
                }
                tx.commit();
            }
        }

        @Override
        public void setChanged() {}

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public void clearContent() {
            for (int i = 0; i < storage.getSlotCount(); i++) {
                setItem(i, ItemStack.EMPTY);
            }
        }
    }
}
