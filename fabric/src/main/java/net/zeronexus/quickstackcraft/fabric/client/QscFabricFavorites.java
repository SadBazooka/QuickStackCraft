package net.zeronexus.quickstackcraft.fabric.client;

import dev.architectury.networking.NetworkManager;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.zeronexus.quickstackcraft.client.ClientFavoritesCache;
import net.zeronexus.quickstackcraft.fabric.mixin.AbstractContainerScreenAccessor;
import net.zeronexus.quickstackcraft.network.FavoriteToggleC2SPacket;

/**
 * Fabric equivalent of the NeoForge {@code QscFavoritesRenderer}: draws the gold favorite overlay
 * on the player's inventory slots in every container screen, and toggles a slot's favorite state on
 * Alt + left-click. Uses Fabric's per-screen {@link ScreenEvents} / {@link ScreenMouseEvents}.
 */
public final class QscFabricFavorites {

    private static final int FAVORITE_OVERLAY = 0x40FFD700; // 25% gold

    private QscFabricFavorites() {}

    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof AbstractContainerScreen<?>)) return;
            ScreenEvents.afterRender(screen).register(
                    (scr, graphics, mouseX, mouseY, tickDelta) -> renderOverlay((AbstractContainerScreen<?>) scr, graphics));
            ScreenMouseEvents.allowMouseClick(screen).register(
                    (scr, mouseX, mouseY, button) -> !tryToggle((AbstractContainerScreen<?>) scr, mouseX, mouseY, button));
        });
    }

    private static void renderOverlay(AbstractContainerScreen<?> screen, GuiGraphics graphics) {
        AbstractContainerScreenAccessor acc = (AbstractContainerScreenAccessor) screen;
        int left = acc.quickstackcraft$getLeftPos();
        int top = acc.quickstackcraft$getTopPos();
        for (Slot slot : screen.getMenu().slots) {
            if (slot.container instanceof Inventory && ClientFavoritesCache.isFavorited(slot.getContainerSlot())) {
                int x = left + slot.x;
                int y = top + slot.y;
                graphics.fill(x, y, x + 16, y + 16, FAVORITE_OVERLAY);
            }
        }
    }

    /** @return true if an Alt+click toggled a favorite (so the click should be cancelled). */
    private static boolean tryToggle(AbstractContainerScreen<?> screen, double mouseX, double mouseY, int button) {
        if (button != 0 || !Screen.hasAltDown()) return false;

        AbstractContainerScreenAccessor acc = (AbstractContainerScreenAccessor) screen;
        int left = acc.quickstackcraft$getLeftPos();
        int top = acc.quickstackcraft$getTopPos();
        for (Slot slot : screen.getMenu().slots) {
            if (!(slot.container instanceof Inventory)) continue;
            int x = left + slot.x;
            int y = top + slot.y;
            if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                int index = slot.getContainerSlot();
                if (index >= 0 && index < 36) {
                    NetworkManager.sendToServer(new FavoriteToggleC2SPacket(index));
                    return true;
                }
                return false;
            }
        }
        return false;
    }
}
