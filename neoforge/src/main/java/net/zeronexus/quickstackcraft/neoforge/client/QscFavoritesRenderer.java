package net.zeronexus.quickstackcraft.neoforge.client;

import dev.architectury.networking.NetworkManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.zeronexus.quickstackcraft.client.ClientFavoritesCache;
import net.zeronexus.quickstackcraft.network.FavoriteToggleC2SPacket;

/**
 * Favorite-slot handling for <em>every</em> container screen — chests, backpacks, machines, etc. —
 * not just the player inventory screen:
 * <ul>
 *   <li>draws the gold favorite overlay on the player's inventory slots ({@link ScreenEvent.Render.Post});</li>
 *   <li>toggles a slot's favorite state on Alt + left-click ({@link ScreenEvent.MouseButtonPressed.Pre}).</li>
 * </ul>
 *
 * <p>Uses NeoForge screen events instead of a mixin so it applies uniformly to any
 * {@link AbstractContainerScreen} regardless of which mod owns it.
 */
public final class QscFavoritesRenderer {

    private static final int FAVORITE_OVERLAY = 0x40FFD700; // 25% gold

    private QscFavoritesRenderer() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(QscFavoritesRenderer::onRenderPost);
        NeoForge.EVENT_BUS.addListener(QscFavoritesRenderer::onMousePressed);
    }

    private static void onRenderPost(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) return;

        GuiGraphics graphics = event.getGuiGraphics();
        int left = screen.getGuiLeft();
        int top = screen.getGuiTop();

        for (Slot slot : screen.getMenu().slots) {
            if (slot.container instanceof Inventory && ClientFavoritesCache.isFavorited(slot.getContainerSlot())) {
                int x = left + slot.x;
                int y = top + slot.y;
                graphics.fill(x, y, x + 16, y + 16, FAVORITE_OVERLAY);
            }
        }
    }

    private static void onMousePressed(ScreenEvent.MouseButtonPressed.Pre event) {
        if (event.getButton() != 0 || !Screen.hasAltDown()) return; // left-click + Alt only
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) return;

        int left = screen.getGuiLeft();
        int top = screen.getGuiTop();
        double mx = event.getMouseX();
        double my = event.getMouseY();

        for (Slot slot : screen.getMenu().slots) {
            if (!(slot.container instanceof Inventory)) continue;
            int x = left + slot.x;
            int y = top + slot.y;
            if (mx >= x && mx < x + 16 && my >= y && my < y + 16) {
                int index = slot.getContainerSlot();
                if (index >= 0 && index < 36) {
                    NetworkManager.sendToServer(new FavoriteToggleC2SPacket(index));
                    event.setCanceled(true); // don't also pick up / move the stack
                }
                return;
            }
        }
    }
}
