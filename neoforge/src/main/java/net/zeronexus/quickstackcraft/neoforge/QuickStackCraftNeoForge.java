package net.zeronexus.quickstackcraft.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.zeronexus.quickstackcraft.QuickStackCraft;
import net.zeronexus.quickstackcraft.client.ModKeybinds;
import net.zeronexus.quickstackcraft.network.ModNetworking;
import net.minecraft.server.level.ServerPlayer;

@Mod(QuickStackCraft.MOD_ID)
public class QuickStackCraftNeoForge {

    public QuickStackCraftNeoForge(IEventBus modEventBus) {
        QuickStackCraft.init();

        // Register attachment types
        FavoritesManagerImpl.ATTACHMENTS.register(modEventBus);

        // Register keybinds
        modEventBus.addListener(this::registerKeybinds);

        // Game event: sync favorites on login
        NeoForge.EVENT_BUS.addListener(this::onPlayerLogin);
    }

    private void registerKeybinds(RegisterKeyMappingsEvent event) {
        event.register(ModKeybinds.QUICK_STACK);
        event.register(ModKeybinds.DUMP_ALL);
    }

    private void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            ModNetworking.syncFavoritesToPlayer(sp);
        }
    }
}
