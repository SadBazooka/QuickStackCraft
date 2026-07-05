package net.zeronexus.quickstackcraft.fabric.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Mod Menu entry point: adds a "Config" button to the mod's Mod Menu entry that opens the Cloth
 * Config screen. Only wired when this class is loaded (Mod Menu invokes the "modmenu" entrypoint),
 * and only returns a real screen when Cloth Config is also present.
 */
public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if (!FabricLoader.getInstance().isModLoaded("cloth-config")) {
            return parent -> null; // no Cloth Config -> no config button
        }
        return ClothConfigScreen::create;
    }
}
