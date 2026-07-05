package net.zeronexus.quickstackcraft.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.zeronexus.quickstackcraft.QuickStackCraft;
import net.zeronexus.quickstackcraft.client.ModKeybinds;
import net.zeronexus.quickstackcraft.fabric.client.QscFabricFavorites;
import net.zeronexus.quickstackcraft.fabric.client.QscFabricInput;
import net.zeronexus.quickstackcraft.fabric.client.SbBackpackButtonsFabric;

public class QuickStackCraftFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(ModKeybinds.QUICK_STACK);
        KeyBindingHelper.registerKeyBinding(ModKeybinds.DUMP_ALL);
        KeyBindingHelper.registerKeyBinding(ModKeybinds.PREVIEW_TARGETS);
        KeyBindingHelper.registerKeyBinding(ModKeybinds.TOGGLE_TARGET);

        QscFabricInput.register();     // world keybind polling
        QscFabricFavorites.register(); // favorite highlight + Alt-click toggle on all container screens

        // Sophisticated Backpacks (Fabric port) integration - only when the mod is present.
        if (FabricLoader.getInstance().isModLoaded("sophisticatedbackpacks")) {
            SbBackpackButtonsFabric.register();
        }

        QuickStackCraft.initClient();
    }
}
