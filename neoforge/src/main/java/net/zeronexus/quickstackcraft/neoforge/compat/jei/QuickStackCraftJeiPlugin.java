package net.zeronexus.quickstackcraft.neoforge.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.resources.ResourceLocation;
import net.zeronexus.quickstackcraft.QuickStackCraft;
import net.zeronexus.quickstackcraft.compat.jei.CraftFromNearbyTransferHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NeoForge-side JEI plugin entry point. Must be in the platform module
 * (not common) for NeoForge's ModFileScanData to discover @JeiPlugin.
 */
@JeiPlugin
public class QuickStackCraftJeiPlugin implements IModPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger("QuickStackCraft-JEI");
    private static final ResourceLocation PLUGIN_ID =
            ResourceLocation.fromNamespaceAndPath(QuickStackCraft.MOD_ID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        LOGGER.info("[QuickStackCraft] Registering craft-from-nearby transfer handler");
        registration.addRecipeTransferHandler(
                new CraftFromNearbyTransferHandler(),
                RecipeTypes.CRAFTING
        );
    }
}
