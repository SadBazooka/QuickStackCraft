package net.zeronexus.quickstackcraft.compat.jei;

import dev.architectury.networking.NetworkManager;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
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
 */
public class CraftFromNearbyTransferHandler implements IRecipeTransferHandler<InventoryMenu, RecipeHolder<CraftingRecipe>> {

    @Override
    public Class<InventoryMenu> getContainerClass() {
        return InventoryMenu.class;
    }

    @Override
    public Optional<MenuType<InventoryMenu>> getMenuType() {
        return Optional.empty(); // InventoryMenu is always open, no MenuType
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

        if (!doTransfer) {
            // Just checking if transfer is possible - always show the "+" button
            return null;
        }

        // Extract the displayed ingredients (JEI resolves tags to concrete items)
        List<ItemStack> ingredients = new ArrayList<>();
        recipeSlots.getSlotViews(RecipeIngredientRole.INPUT).forEach(slotView -> {
            ItemStack displayed = slotView.getDisplayedIngredient()
                    .flatMap(typed -> typed.getIngredient() instanceof ItemStack stack
                            ? Optional.of(stack) : Optional.empty())
                    .orElse(ItemStack.EMPTY);
            ingredients.add(displayed.copy());
        });

        // Pad to 9 slots (3x3 crafting grid)
        while (ingredients.size() < 9) {
            ingredients.add(ItemStack.EMPTY);
        }

        NetworkManager.sendToServer(new CraftFromNearbyC2SPacket(ingredients));
        return null;
    }
}
