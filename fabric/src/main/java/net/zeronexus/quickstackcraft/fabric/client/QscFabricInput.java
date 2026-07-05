package net.zeronexus.quickstackcraft.fabric.client;

import dev.architectury.networking.NetworkManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.zeronexus.quickstackcraft.client.ModKeybinds;
import net.zeronexus.quickstackcraft.client.compat.itemlocks.ItemLocksCompat;
import net.zeronexus.quickstackcraft.network.DumpC2SPacket;
import net.zeronexus.quickstackcraft.network.PreviewTargetsC2SPacket;
import net.zeronexus.quickstackcraft.network.QuickStackC2SPacket;
import net.zeronexus.quickstackcraft.network.ToggleTargetC2SPacket;

/**
 * Fabric equivalent of the NeoForge client-tick keybind poller: when the player is in the world
 * (no screen open), fire the matching action for each mod keybind.
 */
public final class QscFabricInput {

    private QscFabricInput() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(QscFabricInput::onEndTick);
    }

    private static void onEndTick(Minecraft mc) {
        if (mc.player == null || mc.screen != null) return;

        while (ModKeybinds.QUICK_STACK.consumeClick()) {
            NetworkManager.sendToServer(new QuickStackC2SPacket(ItemLocksCompat.lockedMask()));
        }
        while (ModKeybinds.DUMP_ALL.consumeClick()) {
            NetworkManager.sendToServer(new DumpC2SPacket(ItemLocksCompat.lockedMask()));
        }
        while (ModKeybinds.PREVIEW_TARGETS.consumeClick()) {
            NetworkManager.sendToServer(new PreviewTargetsC2SPacket());
        }
        while (ModKeybinds.TOGGLE_TARGET.consumeClick()) {
            NetworkManager.sendToServer(new ToggleTargetC2SPacket(false));
        }
    }
}
