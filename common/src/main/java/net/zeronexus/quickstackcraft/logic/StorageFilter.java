package net.zeronexus.quickstackcraft.logic;

import net.minecraft.core.registries.BuiltInRegistries;
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
 * <p>In {@code STORAGE_ONLY} mode this is a strict <em>allowlist</em>: a block must be recognised
 * storage (by tag, vanilla type, the shipped {@code quickstackcraft:storage} tag, or the user's
 * whitelist) to receive items. Everything else - machines, generators, furnaces, trash cans - is
 * rejected by default, so items can't be consumed or lost. The old "looks big enough" slot-count
 * heuristic is opt-in via {@link QscConfig#allowUnrecognizedBySlotCount}.
 */
public final class StorageFilter {

    private StorageFilter() {}

    // Common (cross-mod) storage tags. Modded storage (Sophisticated Storage, etc.) declares these.
    private static final TagKey<Block> TAG_CHESTS =
            TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", "chests"));
    private static final TagKey<Block> TAG_BARRELS =
            TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", "barrels"));
    private static final TagKey<Block> TAG_SHULKERS =
            TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", "shulker_boxes"));
    // Our own tag - shipped pre-filled with common untagged storage (drawers, ender storage, ...)
    // and extendable by packs/datapacks.
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

        // Explicit overrides (config + in-game) always win. Whitelist beats blacklist.
        if (id != null && (QscConfig.whitelist.contains(id) || RuntimeTargets.isWhitelisted(id))) return true;
        if (id != null && (QscConfig.blacklist.contains(id) || RuntimeTargets.isBlacklisted(id))) return false;

        if (QscConfig.storageMode == QscConfig.StorageMode.ALL_CONTAINERS) {
            return true;
        }

        // --- STORAGE_ONLY: strict allowlist ---

        // Recognised storage by tag (covers vanilla + most modded storage).
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

        // Opt-in fallback: treat any large unrecognised inventory as storage. OFF by default, since
        // machines (e.g. Iron Furnaces) expose many slots and would be wrongly accepted.
        if (QscConfig.allowUnrecognizedBySlotCount) {
            return container.getContainerSize() >= QscConfig.minSlots;
        }

        return false;
    }

    private static ResourceLocation blockId(BlockState state) {
        return BuiltInRegistries.BLOCK.getKey(state.getBlock());
    }
}
