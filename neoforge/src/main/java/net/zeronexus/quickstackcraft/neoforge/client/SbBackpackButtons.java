package net.zeronexus.quickstackcraft.neoforge.client;

import dev.architectury.networking.NetworkManager;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.p3pp3rf1y.sophisticatedbackpacks.client.gui.BackpackScreen;
import net.zeronexus.quickstackcraft.client.ModKeybinds;
import net.zeronexus.quickstackcraft.network.StorageTransferC2SPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds Quick Stack / Dump controls to the Sophisticated Backpacks screen via NeoForge's
 * {@link ScreenEvent}, rather than a mixin. The event hook is hierarchy-independent and fires
 * for every screen, so it doesn't depend on matching a specific injection point.
 *
 * <p>This class references Sophisticated Backpacks classes directly, so it is only loaded and
 * registered when the mod is present (see the guard in {@code QuickStackCraftNeoForge}).
 */
public final class SbBackpackButtons {

    private static final Logger LOGGER = LoggerFactory.getLogger("QuickStackCraft-SB");

    private SbBackpackButtons() {}

    /** Register the screen + key listeners on the game event bus (client only). */
    public static void register() {
        NeoForge.EVENT_BUS.addListener(SbBackpackButtons::onScreenInit);
        NeoForge.EVENT_BUS.addListener(SbBackpackButtons::onKeyPressed);
        LOGGER.info("[QuickStackCraft] Sophisticated Backpacks integration enabled (screen-event hook)");
    }

    private static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof BackpackScreen screen)) return;

        AbstractContainerScreen<?> acs = (AbstractContainerScreen<?>) screen;
        int left = acs.getGuiLeft();
        int top = acs.getGuiTop();

        int btnSize = 14;
        // Place the two buttons side by side in the empty strip ABOVE the title bar, anchored to
        // the panel's top-left. This is clear of SB's title-bar search box, the right-side
        // setting/sort buttons, and the left-side upgrade slots. Computed from the live
        // getGuiLeft/getGuiTop (scaled coords), so it tracks backpack size and GUI scale.
        int btnX = left + 2;
        int btnY = top - btnSize - 2;
        if (btnY < 2) btnY = 2; // never run off the top edge of the screen

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

        event.addListener(quickStack);
        event.addListener(dump);
    }

    private static void onKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        if (!(event.getScreen() instanceof BackpackScreen)) return;
        if (ModKeybinds.QUICK_STACK.matches(event.getKeyCode(), event.getScanCode())) {
            NetworkManager.sendToServer(new StorageTransferC2SPacket(false));
            event.setCanceled(true);
        } else if (ModKeybinds.DUMP_ALL.matches(event.getKeyCode(), event.getScanCode())) {
            NetworkManager.sendToServer(new StorageTransferC2SPacket(true));
            event.setCanceled(true);
        }
    }
}
