package net.zeronexus.quickstackcraft.fabric.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes {@code leftPos} / {@code topPos} on Fabric. NeoForge adds public
 * {@code getGuiLeft()} / {@code getGuiTop()} getters, but vanilla (and thus Fabric) only has the
 * protected fields, so we need an accessor to read them from outside a subclass.
 */
@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
    @Accessor("leftPos")
    int quickstackcraft$getLeftPos();

    @Accessor("topPos")
    int quickstackcraft$getTopPos();
}
