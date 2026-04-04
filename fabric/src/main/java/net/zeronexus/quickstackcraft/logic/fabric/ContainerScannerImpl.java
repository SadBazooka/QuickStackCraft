package net.zeronexus.quickstackcraft.logic.fabric;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;

/**
 * Fabric implementation: most Fabric mods implement Container directly.
 * Fabric Transfer API support can be added later if needed.
 */
public final class ContainerScannerImpl {

    private ContainerScannerImpl() {}

    public static Container getContainerFromCapability(Level level, BlockPos pos) {
        return null;
    }
}
