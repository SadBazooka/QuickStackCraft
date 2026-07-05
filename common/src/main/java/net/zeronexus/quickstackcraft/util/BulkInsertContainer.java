package net.zeronexus.quickstackcraft.util;

import net.minecraft.world.item.ItemStack;

/**
 * Optional capability for a {@link net.minecraft.world.Container} that can insert items itself,
 * more correctly/efficiently than the generic slot-by-slot fill in {@link ContainerAccess}.
 *
 * <p>Implemented by the Fabric Transfer-API storage adapter, which inserts via a transaction (so
 * capacity limits and multi-slot distribution are handled by the storage, with no risk of losing
 * items). {@link ContainerAccess#insertItem(ItemStack)} delegates here when available.
 */
public interface BulkInsertContainer {

    /**
     * Insert as much of {@code stack} as fits.
     * @return the remainder that could not be inserted (empty if all fit)
     */
    ItemStack quickstackcraft$insertBulk(ItemStack stack);
}
