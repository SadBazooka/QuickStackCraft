package net.zeronexus.quickstackcraft.fabric.client;

import dev.architectury.networking.NetworkManager;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.p3pp3rf1y.sophisticatedbackpacks.client.gui.BackpackScreen;
import net.zeronexus.quickstackcraft.client.ModKeybinds;
import net.zeronexus.quickstackcraft.fabric.mixin.AbstractContainerScreenAccessor;
import net.zeronexus.quickstackcraft.network.StorageTransferC2SPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fabric equivalent of the NeoForge {@code SbBackpackButtons}: adds Quick Stack / Dump controls to
 * the Sophisticated Backpacks screen (buttons above the title bar + keybinds while it's open),
 * moving the backpack's contents into nearby world storage.
 *
 * <p>References Sophisticated Backpacks classes directly, so it is only registered when that mod is
 * present (guarded in the client initializer).
 */
public final class SbBackpackButtonsFabric {

    private static final Logger LOGGER = LoggerFactory.getLogger("QuickStackCraft-SB");

    private SbBackpackButtonsFabric() {}

    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof BackpackScreen)) return;

            addButtons(screen);

            ScreenKeyboardEvents.allowKeyPress(screen).register((scr, key, scancode, modifiers) -> {
                if (ModKeybinds.QUICK_STACK.matches(key, scancode)) {
                    NetworkManager.sendToServer(new StorageTransferC2SPacket(false));
                    return false;
                } else if (ModKeybinds.DUMP_ALL.matches(key, scancode)) {
                    NetworkManager.sendToServer(new StorageTransferC2SPacket(true));
                    return false;
                }
                return true;
            });
        });
        LOGGER.info("[QuickStackCraft] Sophisticated Backpacks integration enabled (Fabric)");
    }

    private static void addButtons(Screen screen) {
        AbstractContainerScreenAccessor acc = (AbstractContainerScreenAccessor) screen;
        int left = acc.quickstackcraft$getLeftPos();
        int top = acc.quickstackcraft$getTopPos();

        int btnSize = 14;
        // Empty strip above the title bar; always on-screen regardless of backpack size.
        int btnX = left + 2;
        int btnY = top - btnSize - 2;
        if (btnY < 2) btnY = 2;

        Button quickStack = Button.builder(Component.literal("Q"),
                        b -> NetworkManager.sendToServer(new StorageTransferC2SPacket(false)))
                .bounds(btnX, btnY, btnSize, btnSize)
                .tooltip(Tooltip.create(Component.translatable("quickstackcraft.button.quick_stack")))
                .build();

        Button dump = Button.builder(Component.literal("D"),
                        b -> NetworkManager.sendToServer(new StorageTransferC2SPacket(true)))
                .bounds(btnX + btnSize + 2, btnY, btnSize, btnSize)
                .tooltip(Tooltip.create(Component.translatable("quickstackcraft.button.dump_all")))
                .build();

        Screens.getButtons(screen).add(quickStack);
        Screens.getButtons(screen).add(dump);
    }
}
