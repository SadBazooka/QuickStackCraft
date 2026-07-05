package net.zeronexus.quickstackcraft.network;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.zeronexus.quickstackcraft.QuickStackCraft;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.zeronexus.quickstackcraft.client.ClientFavoritesCache;
import net.zeronexus.quickstackcraft.client.ContainerHighlightRenderer;
import net.zeronexus.quickstackcraft.client.NearbyItemsCache;
import net.zeronexus.quickstackcraft.config.QscConfig;
import net.zeronexus.quickstackcraft.logic.ContainerScanner;
import net.zeronexus.quickstackcraft.logic.CraftFromNearbyLogic;
import net.zeronexus.quickstackcraft.logic.DumpLogic;
import net.zeronexus.quickstackcraft.logic.FavoritesManager;
import net.zeronexus.quickstackcraft.logic.QuickStackLogic;
import net.zeronexus.quickstackcraft.logic.RuntimeTargets;
import net.zeronexus.quickstackcraft.logic.TransferResult;
import net.zeronexus.quickstackcraft.util.ContainerAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ModNetworking {

    private static final boolean DEFAULT_SKIP_HOTBAR = true;
    private static final boolean DEFAULT_INCLUDE_ENTITIES = true;

    private ModNetworking() {}

    public static void register() {
        // C2S: Quick Stack
        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                QuickStackC2SPacket.TYPE,
                QuickStackC2SPacket.CODEC,
                ModNetworking::handleQuickStack
        );

        // C2S: Dump All
        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                DumpC2SPacket.TYPE,
                DumpC2SPacket.CODEC,
                ModNetworking::handleDump
        );

        // C2S: Craft From Nearby
        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                CraftFromNearbyC2SPacket.TYPE,
                CraftFromNearbyC2SPacket.CODEC,
                ModNetworking::handleCraftFromNearby
        );

        // C2S: Stack/Dump from open storage (e.g. Sophisticated Backpacks)
        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                StorageTransferC2SPacket.TYPE,
                StorageTransferC2SPacket.CODEC,
                ModNetworking::handleStorageTransfer
        );

        // C2S: Toggle looked-at block in whitelist/blacklist
        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                ToggleTargetC2SPacket.TYPE,
                ToggleTargetC2SPacket.CODEC,
                ModNetworking::handleToggleTarget
        );

        // C2S: Preview eligible storage targets (highlights, no transfer)
        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                PreviewTargetsC2SPacket.TYPE,
                PreviewTargetsC2SPacket.CODEC,
                ModNetworking::handlePreviewTargets
        );

        // C2S: Toggle Favorite
        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                FavoriteToggleC2SPacket.TYPE,
                FavoriteToggleC2SPacket.CODEC,
                ModNetworking::handleFavoriteToggle
        );

        // S2C: Sync Favorites
        NetworkManager.registerReceiver(
                NetworkManager.Side.S2C,
                FavoriteSyncS2CPacket.TYPE,
                FavoriteSyncS2CPacket.CODEC,
                ModNetworking::handleFavoriteSync
        );

        // C2S: Nearby Items Scan (for JEI availability)
        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                NearbyItemsScanC2SPacket.TYPE,
                NearbyItemsScanC2SPacket.CODEC,
                ModNetworking::handleNearbyItemsScan
        );

        // S2C: Nearby Items Sync (response to scan)
        NetworkManager.registerReceiver(
                NetworkManager.Side.S2C,
                NearbyItemsSyncS2CPacket.TYPE,
                NearbyItemsSyncS2CPacket.CODEC,
                ModNetworking::handleNearbyItemsSync
        );

        // S2C: Container Highlights
        NetworkManager.registerReceiver(
                NetworkManager.Side.S2C,
                ContainerHighlightS2CPacket.TYPE,
                ContainerHighlightS2CPacket.CODEC,
                ModNetworking::handleContainerHighlight
        );
    }

    private static void handleQuickStack(QuickStackC2SPacket packet, NetworkManager.PacketContext context) {
        context.queue(() -> {
            ServerPlayer player = (ServerPlayer) context.getPlayer();
            Vec3 center = player.position();

            List<ContainerAccess> containers = ContainerScanner.findNearby(
                    player.level(), center, QscConfig.radius, DEFAULT_INCLUDE_ENTITIES);

            TransferResult result = QuickStackLogic.execute(
                    player, containers, DEFAULT_SKIP_HOTBAR,
                    slot -> FavoritesManager.isFavorited(player, slot) || packet.isLocked(slot));

            player.containerMenu.broadcastChanges();

            if (result.didSomething()) {
                player.displayClientMessage(
                        Component.translatable("quickstackcraft.message.quick_stack",
                                result.itemsMoved(), result.containersUsed()),
                        true);
                sendHighlights(player, result);
            } else {
                player.displayClientMessage(
                        Component.translatable("quickstackcraft.message.nothing_to_stack"),
                        true);
            }
        });
    }

    private static void handleDump(DumpC2SPacket packet, NetworkManager.PacketContext context) {
        context.queue(() -> {
            ServerPlayer player = (ServerPlayer) context.getPlayer();
            Vec3 center = player.position();

            List<ContainerAccess> containers = ContainerScanner.findNearby(
                    player.level(), center, QscConfig.radius, DEFAULT_INCLUDE_ENTITIES);

            TransferResult result = DumpLogic.execute(
                    player, containers, DEFAULT_SKIP_HOTBAR,
                    slot -> FavoritesManager.isFavorited(player, slot) || packet.isLocked(slot));

            player.containerMenu.broadcastChanges();

            if (result.didSomething()) {
                player.displayClientMessage(
                        Component.translatable("quickstackcraft.message.dump",
                                result.itemsMoved(), result.containersUsed()),
                        true);
                sendHighlights(player, result);
            } else {
                player.displayClientMessage(
                        Component.translatable("quickstackcraft.message.nothing_to_dump"),
                        true);
            }
        });
    }

    private static void handleFavoriteToggle(FavoriteToggleC2SPacket packet, NetworkManager.PacketContext context) {
        context.queue(() -> {
            ServerPlayer player = (ServerPlayer) context.getPlayer();
            int slot = packet.slot();

            // Validate slot range (main inventory: 0-35)
            if (slot < 0 || slot >= 36) return;

            FavoritesManager.toggle(player, slot);

            // Sync updated favorites back to client
            Set<Integer> favorites = FavoritesManager.getAll(player);
            NetworkManager.sendToPlayer(player, new FavoriteSyncS2CPacket(favorites));
        });
    }

    private static void handleFavoriteSync(FavoriteSyncS2CPacket packet, NetworkManager.PacketContext context) {
        context.queue(() -> {
            ClientFavoritesCache.update(packet.slots());
        });
    }

    private static void handleCraftFromNearby(CraftFromNearbyC2SPacket packet, NetworkManager.PacketContext context) {
        context.queue(() -> {
            ServerPlayer player = (ServerPlayer) context.getPlayer();
            Vec3 center = player.position();

            List<ContainerAccess> containers = ContainerScanner.findNearby(
                    player.level(), center, QscConfig.radius, DEFAULT_INCLUDE_ENTITIES);

            // Detect which menu is open and get the appropriate crafting grid slots
            List<net.minecraft.world.inventory.Slot> craftSlots = new java.util.ArrayList<>();
            net.minecraft.world.inventory.AbstractContainerMenu activeMenu;

            if (player.containerMenu instanceof net.minecraft.world.inventory.CraftingMenu craftingMenu) {
                // 3x3 crafting table: slots 1-9 (slot 0 is result)
                activeMenu = craftingMenu;
                for (int i = 1; i <= 9; i++) {
                    craftSlots.add(craftingMenu.getSlot(i));
                }
            } else {
                // 2x2 player inventory crafting: slots 1-4 (slot 0 is result)
                activeMenu = player.inventoryMenu;
                for (int i = 1; i <= 4; i++) {
                    craftSlots.add(player.inventoryMenu.getSlot(i));
                }
            }

            CraftFromNearbyLogic.CraftResult result = CraftFromNearbyLogic.execute(
                    player, packet.slotOptions(), craftSlots, containers);

            activeMenu.broadcastChanges();

            if (result.isComplete()) {
                player.displayClientMessage(
                        Component.translatable("quickstackcraft.message.craft_ready",
                                result.placed()),
                        true);
            } else if (result.placed() > 0) {
                player.displayClientMessage(
                        Component.translatable("quickstackcraft.message.craft_partial",
                                result.placed(), result.needed()),
                        true);
            } else {
                player.displayClientMessage(
                        Component.translatable("quickstackcraft.message.craft_no_ingredients"),
                        true);
            }
        });
    }

    private static void handleStorageTransfer(StorageTransferC2SPacket packet, NetworkManager.PacketContext context) {
        context.queue(() -> {
            ServerPlayer player = (ServerPlayer) context.getPlayer();

            List<net.minecraft.world.inventory.Slot> sourceSlots =
                    net.zeronexus.quickstackcraft.logic.OpenStorageAccess.getOpenStorageSlots(player);
            if (sourceSlots.isEmpty()) {
                return; // No supported storage open; nothing to do.
            }

            List<ContainerAccess> containers = ContainerScanner.findNearby(
                    player.level(), player.position(), QscConfig.radius, DEFAULT_INCLUDE_ENTITIES);

            TransferResult result = net.zeronexus.quickstackcraft.logic.SlotTransferLogic.execute(
                    sourceSlots, containers, packet.dumpAll());

            player.containerMenu.broadcastChanges();

            if (result.didSomething()) {
                player.displayClientMessage(
                        Component.translatable(
                                packet.dumpAll() ? "quickstackcraft.message.dump" : "quickstackcraft.message.quick_stack",
                                result.itemsMoved(), result.containersUsed()),
                        true);
                sendHighlights(player, result);
            } else {
                player.displayClientMessage(
                        Component.translatable(
                                packet.dumpAll() ? "quickstackcraft.message.nothing_to_dump" : "quickstackcraft.message.nothing_to_stack"),
                        true);
            }
        });
    }

    private static void handleToggleTarget(ToggleTargetC2SPacket packet, NetworkManager.PacketContext context) {
        context.queue(() -> {
            ServerPlayer player = (ServerPlayer) context.getPlayer();
            // The lists are server-wide, so gate edits behind op permission (always true in singleplayer).
            if (!player.hasPermissions(2)) {
                player.displayClientMessage(Component.translatable("quickstackcraft.message.no_permission"), true);
                return;
            }

            BlockHitResult hit = raycastBlock(player);
            if (hit.getType() != HitResult.Type.BLOCK) {
                player.displayClientMessage(Component.translatable("quickstackcraft.message.no_block"), true);
                return;
            }

            BlockState state = player.level().getBlockState(hit.getBlockPos());
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
            Component name = state.getBlock().getName();

            if (packet.blacklist()) {
                boolean added;
                if (RuntimeTargets.isBlacklisted(id)) { RuntimeTargets.removeBlacklist(id); added = false; }
                else { RuntimeTargets.addBlacklist(id); added = true; }
                player.displayClientMessage(Component.translatable(
                        added ? "quickstackcraft.message.blacklist_added" : "quickstackcraft.message.blacklist_removed", name), true);
            } else {
                boolean added;
                if (RuntimeTargets.isWhitelisted(id)) { RuntimeTargets.removeWhitelist(id); added = false; }
                else { RuntimeTargets.addWhitelist(id); added = true; }
                player.displayClientMessage(Component.translatable(
                        added ? "quickstackcraft.message.whitelist_added" : "quickstackcraft.message.whitelist_removed", name), true);
            }
        });
    }

    private static void handlePreviewTargets(PreviewTargetsC2SPacket packet, NetworkManager.PacketContext context) {
        context.queue(() -> {
            ServerPlayer player = (ServerPlayer) context.getPlayer();
            List<ContainerAccess> containers = ContainerScanner.findNearby(
                    player.level(), player.position(), QscConfig.radius, DEFAULT_INCLUDE_ENTITIES);

            List<BlockPos> blocks = new java.util.ArrayList<>();
            List<Integer> entities = new java.util.ArrayList<>();
            for (ContainerAccess ca : containers) {
                if (ca.isBlockContainer()) blocks.add(ca.blockPos());
                else if (ca.entity() != null) entities.add(ca.entity().getId());
            }

            if (blocks.isEmpty() && entities.isEmpty()) {
                player.displayClientMessage(Component.translatable("quickstackcraft.message.no_targets"), true);
            } else {
                NetworkManager.sendToPlayer(player, new ContainerHighlightS2CPacket(blocks, entities));
                player.displayClientMessage(Component.translatable("quickstackcraft.message.preview", blocks.size() + entities.size()), true);
            }
        });
    }

    /** Raytrace the block the player is looking at (server-side). */
    private static BlockHitResult raycastBlock(ServerPlayer player) {
        Vec3 eye = player.getEyePosition(1.0f);
        Vec3 end = eye.add(player.getViewVector(1.0f).scale(6.0));
        return player.level().clip(new ClipContext(eye, end,
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
    }

    private static void handleNearbyItemsScan(NearbyItemsScanC2SPacket packet, NetworkManager.PacketContext context) {
        context.queue(() -> {
            ServerPlayer player = (ServerPlayer) context.getPlayer();
            List<ContainerAccess> containers = ContainerScanner.findNearby(
                    player.level(), player.position(), QscConfig.radius, DEFAULT_INCLUDE_ENTITIES);

            Map<Item, Integer> available = new HashMap<>();
            for (ContainerAccess ca : containers) {
                Container c = ca.container();
                for (int i = 0; i < c.getContainerSize(); i++) {
                    ItemStack stack = c.getItem(i);
                    if (!stack.isEmpty()) {
                        available.merge(stack.getItem(), stack.getCount(), Integer::sum);
                    }
                }
            }

            NetworkManager.sendToPlayer(player, new NearbyItemsSyncS2CPacket(available));
        });
    }

    private static void handleNearbyItemsSync(NearbyItemsSyncS2CPacket packet, NetworkManager.PacketContext context) {
        context.queue(() -> {
            NearbyItemsCache.update(packet.items());
        });
    }

    private static void handleContainerHighlight(ContainerHighlightS2CPacket packet, NetworkManager.PacketContext context) {
        context.queue(() -> {
            ContainerHighlightRenderer.onHighlightReceived(packet.blockPositions(), packet.entityIds());
        });
    }

    private static void sendHighlights(ServerPlayer player, TransferResult result) {
        if (!result.blockPositions().isEmpty() || !result.entityIds().isEmpty()) {
            NetworkManager.sendToPlayer(player, new ContainerHighlightS2CPacket(
                    result.blockPositions(), result.entityIds()));
            spawnHighlightParticles(player, result);
        }
    }

    private static void spawnHighlightParticles(ServerPlayer player, TransferResult result) {
        ServerLevel level = player.serverLevel();
        Vec3 playerPos = player.position().add(0, 1, 0); // Chest height

        for (BlockPos pos : result.blockPositions()) {
            Vec3 target = Vec3.atCenterOf(pos);
            // Spawn particles along the trail from player to container
            int steps = (int) Math.max(3, playerPos.distanceTo(target) * 2);
            for (int i = 0; i <= steps; i++) {
                double t = (double) i / steps;
                double x = playerPos.x + (target.x - playerPos.x) * t;
                double y = playerPos.y + (target.y - playerPos.y) * t;
                double z = playerPos.z + (target.z - playerPos.z) * t;
                level.sendParticles(player, ParticleTypes.HAPPY_VILLAGER,
                        true, x, y, z, 1, 0.05, 0.05, 0.05, 0);
            }
        }
    }

    /**
     * Send current favorites to a player (call on login/respawn).
     */
    public static void syncFavoritesToPlayer(ServerPlayer player) {
        Set<Integer> favorites = FavoritesManager.getAll(player);
        NetworkManager.sendToPlayer(player, new FavoriteSyncS2CPacket(favorites));
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(QuickStackCraft.MOD_ID, path);
    }
}
