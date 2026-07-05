package net.zeronexus.quickstackcraft.neoforge.client;

import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.zeronexus.quickstackcraft.client.ModKeybinds;
import net.zeronexus.quickstackcraft.client.compat.itemlocks.ItemLocksCompat;
import net.zeronexus.quickstackcraft.network.DumpC2SPacket;
import net.zeronexus.quickstackcraft.network.PreviewTargetsC2SPacket;
import net.zeronexus.quickstackcraft.network.QuickStackC2SPacket;
import net.zeronexus.quickstackcraft.network.ToggleTargetC2SPacket;

/**
 * Polls the mod's keybinds while the player is in the world (no screen open) and sends the matching
 * action to the server. This makes Quick Stack / Dump / Preview / Toggle-target usable from
 * anywhere, not just inside an inventory GUI.
 */
public final class QscClientInput {

    private QscClientInput() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(QscClientInput::onClientTick);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
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
