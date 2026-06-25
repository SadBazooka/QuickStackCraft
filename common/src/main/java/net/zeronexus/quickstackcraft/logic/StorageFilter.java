package net.zeronexus.quickstackcraft.logic;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.zeronexus.quickstackcraft.config.QscConfig;

/**
 * Decides whether a nearby container is a valid target for dump / quick-stack.
 *
 * <p>The point is to avoid dumping items into machines (e.g. a Mekanism generator's fuel
 * slot), where they would be consumed and lost. See {@link QscConfig} for the tunables.
 */
public final class StorageFilter {

    private StorageFilter() {}

    // Common (cross-mod) storage tags. Modded storage blocks (Sophisticated Storage,
    // Iron Chests, etc.) generally declare these, so they're treated as storage even
    // though we can't reference their classes directly.
    private static final TagKey<Block> TAG_CHESTS =
            TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", "chests"));
    private static final TagKey<Block> TAG_BARRELS =
            TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", "barrels"));
    private static final TagKey<Block> TAG_SHULKERS =
            TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", "shulker_boxes"));
    // Optional pack/user-defined tag for anything else that should always count as storage.
    private static final TagKey<Block> TAG_STORAGE =
            TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("quickstackcraft", "storage"));

    /**
     * @param state     the block state at the container's position
     * @param be        the block entity (may be null for capability-only blocks)
     * @param container the resolved container/inventory being considered
     * @return true if items may be deposited into this container
     */
    public static boolean isEligible(BlockState state, BlockEntity be, Container container) {
        ResourceLocation id = blockId(state);

        // Explicit user overrides always win.
        if (id != null && QscConfig.whitelist.contains(id)) return true;
        if (id != null && QscConfig.blacklist.contains(id)) return false;

        if (QscConfig.storageMode == QscConfig.StorageMode.ALL_CONTAINERS) {
            return true;
        }

        // --- STORAGE_ONLY ---

        // Recognised storage by tag (covers most modded storage).
        if (state.is(TAG_CHESTS) || state.is(TAG_BARRELS) || state.is(TAG_SHULKERS) || state.is(TAG_STORAGE)) {
            return true;
        }

        // Recognised vanilla storage block entities.
        if (be instanceof ChestBlockEntity
                || be instanceof BarrelBlockEntity
                || be instanceof ShulkerBoxBlockEntity
                || be instanceof HopperBlockEntity
                || be instanceof DispenserBlockEntity) { // covers droppers (DropperBlockEntity extends DispenserBlockEntity)
            return true;
        }

        // Fallback heuristic: a large inventory is almost certainly bulk storage, while
        // machines/generators expose only a few item slots. Tunable via config.minSlots.
        return container.getContainerSize() >= QscConfig.minSlots;
    }

    private static ResourceLocation blockId(BlockState state) {
        return net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock());
    }
}
