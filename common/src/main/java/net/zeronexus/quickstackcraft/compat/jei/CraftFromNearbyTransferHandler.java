package net.zeronexus.quickstackcraft.compat.jei;

import dev.architectury.networking.NetworkManager;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.zeronexus.quickstackcraft.network.CraftFromNearbyC2SPacket;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JEI transfer handler: when user clicks "+" on a crafting recipe,
 * sends a packet to the server to pull ingredients from nearby containers.
 * Returns COSMETIC error so the "+" button is always clickable (orange highlight).
 */
public class CraftFromNearbyTransferHandler implements IRecipeTransferHandler<InventoryMenu, RecipeHolder<CraftingRecipe>> {

    private final IRecipeTransferHandlerHelper helper;

    public CraftFromNearbyTransferHandler(IRecipeTransferHandlerHelper helper) {
        this.helper = helper;
    }

    @Override
    public Class<InventoryMenu> getContainerClass() {
        return InventoryMenu.class;
    }

    @Override
    public Optional<MenuType<InventoryMenu>> getMenuType() {
        return Optional.empty();
    }

    @Override
    public RecipeType<RecipeHolder<CraftingRecipe>> getRecipeType() {
        return RecipeTypes.CRAFTING;
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(
            InventoryMenu menu, RecipeHolder<CraftingRecipe> recipe,
            IRecipeSlotsView recipeSlots, Player player,
            boolean maxTransfer, boolean doTransfer) {

        List<ItemStack> ingredients = extractIngredients(recipeSlots);

        if (!doTransfer) {
            // COSMETIC error: "+" button is clickable with green highlight + tooltip
            return new CraftFromNearbyAvailable();
        }

        // Actually do the transfer
        NetworkManager.sendToServer(new CraftFromNearbyC2SPacket(ingredients));
        return null;
    }

    /**
     * COSMETIC error: button is clickable, shows green highlight and tooltip.
     */
    private static class CraftFromNearbyAvailable implements IRecipeTransferError {
        @Override
        public Type getType() {
            return Type.COSMETIC;
        }

        @Override
        public int getButtonHighlightColor() {
            return 0x8040FF40; // Green
        }

        @Override
        public void getTooltip(ITooltipBuilder tooltip) {
            tooltip.add(Component.translatable("quickstackcraft.jei.craft_nearby_tooltip"));
        }
    }

    private List<ItemStack> extractIngredients(IRecipeSlotsView recipeSlots) {
        List<ItemStack> ingredients = new ArrayList<>();
        recipeSlots.getSlotViews(RecipeIngredientRole.INPUT).forEach(slotView -> {
            ItemStack displayed = slotView.getDisplayedIngredient()
                    .flatMap(typed -> typed.getIngredient() instanceof ItemStack stack
                            ? Optional.of(stack) : Optional.empty())
                    .orElse(ItemStack.EMPTY);
            ingredients.add(displayed.copy());
        });
        while (ingredients.size() < 9) {
            ingredients.add(ItemStack.EMPTY);
        }
        return ingredients;
    }
}
