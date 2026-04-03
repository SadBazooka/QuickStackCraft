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
import net.zeronexus.quickstackcraft.client.ClientFavoritesCache;
import net.zeronexus.quickstackcraft.client.ContainerHighlightRenderer;
import net.zeronexus.quickstackcraft.logic.ContainerScanner;
import net.zeronexus.quickstackcraft.logic.CraftFromNearbyLogic;
import net.zeronexus.quickstackcraft.logic.DumpLogic;
import net.zeronexus.quickstackcraft.logic.FavoritesManager;
import net.zeronexus.quickstackcraft.logic.QuickStackLogic;
import net.zeronexus.quickstackcraft.logic.TransferResult;
import net.zeronexus.quickstackcraft.util.ContainerAccess;

import java.util.List;
import java.util.Set;

public final class ModNetworking {

    private static final int DEFAULT_RADIUS = 8;
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
                    player.level(), center, DEFAULT_RADIUS, DEFAULT_INCLUDE_ENTITIES);

            TransferResult result = QuickStackLogic.execute(
                    player, containers, DEFAULT_SKIP_HOTBAR,
                    slot -> FavoritesManager.isFavorited(player, slot));

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
                    player.level(), center, DEFAULT_RADIUS, DEFAULT_INCLUDE_ENTITIES);

            TransferResult result = DumpLogic.execute(
                    player, containers, DEFAULT_SKIP_HOTBAR,
                    slot -> FavoritesManager.isFavorited(player, slot));

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
                    player.level(), center, DEFAULT_RADIUS, DEFAULT_INCLUDE_ENTITIES);

            // Get crafting grid slots from the player's inventory menu (slots 1-4 for 2x2)
            // InventoryMenu crafting slots: index 1-4 (0 is result)
            List<net.minecraft.world.inventory.Slot> craftSlots = new java.util.ArrayList<>();
            for (int i = 1; i <= 4; i++) {
                craftSlots.add(player.inventoryMenu.getSlot(i));
            }

            CraftFromNearbyLogic.CraftResult result = CraftFromNearbyLogic.execute(
                    player, packet.ingredients(), craftSlots, containers);

            player.inventoryMenu.broadcastChanges();

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
