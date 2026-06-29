package net.zeronexus.quickstackcraft.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.zeronexus.quickstackcraft.QuickStackCraft;
import net.zeronexus.quickstackcraft.client.ModKeybinds;
import net.zeronexus.quickstackcraft.logic.neoforge.FavoritesManagerImpl;
import net.zeronexus.quickstackcraft.neoforge.command.QscCommands;
import net.zeronexus.quickstackcraft.neoforge.config.NeoConfig;
import net.zeronexus.quickstackcraft.neoforge.config.RuntimeTargetsStore;
import net.zeronexus.quickstackcraft.network.ModNetworking;
import net.minecraft.server.level.ServerPlayer;

@Mod(QuickStackCraft.MOD_ID)
public class QuickStackCraftNeoForge {

    public QuickStackCraftNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        QuickStackCraft.init();

        // Register the config file (config/quickstackcraft-common.toml) - global and easy to edit.
        modContainer.registerConfig(ModConfig.Type.COMMON, NeoConfig.SPEC);

        // Load the in-game-editable whitelist/blacklist file and wire up saving.
        RuntimeTargetsStore.init();

        // Register attachment types
        FavoritesManagerImpl.ATTACHMENTS.register(modEventBus);

        // Register keybinds and client events
        modEventBus.addListener(this::registerKeybinds);
        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(this::onConfigLoad);
        modEventBus.addListener(this::onConfigReload);

        // Game events
        NeoForge.EVENT_BUS.addListener(this::onPlayerLogin);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        QscCommands.register(event.getDispatcher());
    }

    private void onConfigLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == NeoConfig.SPEC) NeoConfig.bake();
    }

    private void onConfigReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == NeoConfig.SPEC) NeoConfig.bake();
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        QuickStackCraft.initClient();

        // Sophisticated Backpacks integration (client-only, soft dependency). Registering touches
        // SbBackpackButtons, which references SB classes - so only do it when the mod is present.
        if (net.neoforged.fml.ModList.get().isLoaded("sophisticatedbackpacks")) {
            net.zeronexus.quickstackcraft.neoforge.client.SbBackpackButtons.register();
        }

        // World keybind polling (Quick Stack / Dump / Preview / Toggle outside of GUIs)
        net.zeronexus.quickstackcraft.neoforge.client.QscClientInput.register();
    }

    private void registerKeybinds(RegisterKeyMappingsEvent event) {
        event.register(ModKeybinds.QUICK_STACK);
        event.register(ModKeybinds.DUMP_ALL);
        event.register(ModKeybinds.PREVIEW_TARGETS);
        event.register(ModKeybinds.TOGGLE_TARGET);
    }

    private void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            ModNetworking.syncFavoritesToPlayer(sp);
        }
    }
}
