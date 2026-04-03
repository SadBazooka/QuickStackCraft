package net.zeronexus.quickstackcraft;

import dev.architectury.event.events.client.ClientTickEvent;
import net.zeronexus.quickstackcraft.client.ContainerHighlightRenderer;
import net.zeronexus.quickstackcraft.network.ModNetworking;

public final class QuickStackCraft {
    public static final String MOD_ID = "quickstackcraft";

    public static void init() {
        ModNetworking.register();
    }

    public static void initClient() {
        ClientTickEvent.CLIENT_POST.register(mc -> ContainerHighlightRenderer.tick());
    }
}
