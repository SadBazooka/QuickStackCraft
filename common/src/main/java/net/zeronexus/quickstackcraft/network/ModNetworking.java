package net.zeronexus.quickstackcraft.network;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.zeronexus.quickstackcraft.QuickStackCraft;
import net.zeronexus.quickstackcraft.client.ClientFavoritesCache;
import net.zeronexus.quickstackcraft.logic.ContainerScanner;
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
