package net.zeronexus.quickstackcraft.neoforge.mixin;

import dev.architectury.networking.NetworkManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.zeronexus.quickstackcraft.client.ClientFavoritesCache;
import net.zeronexus.quickstackcraft.client.ModKeybinds;
import net.zeronexus.quickstackcraft.network.DumpC2SPacket;
import net.zeronexus.quickstackcraft.network.FavoriteToggleC2SPacket;
import net.zeronexus.quickstackcraft.network.QuickStackC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractContainerScreen<InventoryMenu> {

    private InventoryScreenMixin() { super(null, null, Component.empty()); }

    @Unique private Button quickstackcraft$quickStackButton;
    @Unique private Button quickstackcraft$dumpButton;

    @Inject(method = "init", at = @At("TAIL"))
    private void quickstackcraft$addButtons(CallbackInfo ci) {
        int btnX = this.leftPos + 126;
        int btnY = this.topPos + 62;
        int btnSize = 12;

        quickstackcraft$quickStackButton = Button.builder(Component.literal("Q"), btn -> {
            NetworkManager.sendToServer(new QuickStackC2SPacket());
        }).bounds(btnX, btnY, btnSize, btnSize)
          .tooltip(Tooltip.create(Component.translatable("quickstackcraft.button.quick_stack")))
          .build();

        quickstackcraft$dumpButton = Button.builder(Component.literal("D"), btn -> {
            NetworkManager.sendToServer(new DumpC2SPacket());
        }).bounds(btnX + btnSize + 2, btnY, btnSize, btnSize)
          .tooltip(Tooltip.create(Component.translatable("quickstackcraft.button.dump_all")))
          .build();

        this.addRenderableWidget(quickstackcraft$quickStackButton);
        this.addRenderableWidget(quickstackcraft$dumpButton);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void quickstackcraft$renderFavorites(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        for (Slot slot : this.menu.slots) {
            if (slot.container instanceof Inventory && ClientFavoritesCache.isFavorited(slot.getContainerSlot())) {
                int x = this.leftPos + slot.x;
                int y = this.topPos + slot.y;
                graphics.fill(x, y, x + 16, y + 16, 0x40FFD700);
            }
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void quickstackcraft$onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button == 0 && Screen.hasAltDown()) {
            for (Slot slot : this.menu.slots) {
                if (slot.container instanceof Inventory && this.isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
                    int slotIndex = slot.getContainerSlot();
                    if (slotIndex >= 0 && slotIndex < 36) {
                        NetworkManager.sendToServer(new FavoriteToggleC2SPacket(slotIndex));
                        cir.setReturnValue(true);
                        return;
                    }
                }
            }
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void quickstackcraft$onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (ModKeybinds.QUICK_STACK.matches(keyCode, scanCode)) {
            NetworkManager.sendToServer(new QuickStackC2SPacket());
            cir.setReturnValue(true);
        } else if (ModKeybinds.DUMP_ALL.matches(keyCode, scanCode)) {
            NetworkManager.sendToServer(new DumpC2SPacket());
            cir.setReturnValue(true);
        }
    }
}
