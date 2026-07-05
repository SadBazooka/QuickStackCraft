package net.zeronexus.quickstackcraft.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.zeronexus.quickstackcraft.QuickStackCraft;
import net.zeronexus.quickstackcraft.command.QscCommands;
import net.zeronexus.quickstackcraft.fabric.config.FabricConfig;
import net.zeronexus.quickstackcraft.fabric.config.RuntimeTargetsStore;
import net.zeronexus.quickstackcraft.network.ModNetworking;

public class QuickStackCraftFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        QuickStackCraft.init();

        // Config + in-game-editable target lists
        FabricConfig.load();
        RuntimeTargetsStore.init();

        // Sync favorites to player on login
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                ModNetworking.syncFavoritesToPlayer(handler.getPlayer()));

        // /quickstackcraft commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                QscCommands.register(dispatcher));
    }
}
