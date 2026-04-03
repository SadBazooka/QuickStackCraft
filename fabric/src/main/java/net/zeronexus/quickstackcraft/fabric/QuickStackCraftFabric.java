package net.zeronexus.quickstackcraft.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.zeronexus.quickstackcraft.QuickStackCraft;
import net.zeronexus.quickstackcraft.network.ModNetworking;

public class QuickStackCraftFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        QuickStackCraft.init();

        // Sync favorites to player on login
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ModNetworking.syncFavoritesToPlayer(handler.getPlayer());
        });
    }
}
