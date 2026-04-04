package net.zeronexus.quickstackcraft.compat.jei;

import dev.architectury.networking.NetworkManager;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IUniversalRecipeTransferHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.zeronexus.quickstackcraft.network.CraftFromNearbyC2SPacket;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Universal JEI transfer handler: overrides default "missing items" check.
 * Shows a green "+" button for all crafting recipes, pulling ingredients
 * from nearby containers when clicked.
 */
public class CraftFromNearbyTransferHandler implements IUniversalRecipeTransferHandler<InventoryMenu> {

    @Override
    public Class<InventoryMenu> getContainerClass() {
        return InventoryMenu.class;
    }

    @Override
    public Optional<MenuType<InventoryMenu>> getMenuType() {
        return Optional.empty();
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(
            InventoryMenu container, Object recipe,
            IRecipeSlotsView recipeSlots, Player player,
            boolean maxTransfer, boolean doTransfer) {

        // Only handle recipes that have crafting inputs
        List<ItemStack> ingredients = extractIngredients(recipeSlots);
        if (ingredients.stream().allMatch(ItemStack::isEmpty)) {
            return null; // No inputs, let default handle
        }

        if (!doTransfer) {
            return new CraftFromNearbyAvailable();
        }

        NetworkManager.sendToServer(new CraftFromNearbyC2SPacket(ingredients));
        return null;
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

    private static class CraftFromNearbyAvailable implements IRecipeTransferError {
        @Override
        public Type getType() {
            return Type.COSMETIC;
        }

        @Override
        public int getButtonHighlightColor() {
            return 0x8040FF40;
        }

        @Override
        public void getTooltip(ITooltipBuilder tooltip) {
            tooltip.add(Component.translatable("quickstackcraft.jei.craft_nearby_tooltip"));
        }
    }
}
