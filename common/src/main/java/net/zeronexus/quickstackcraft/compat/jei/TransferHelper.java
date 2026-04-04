package net.zeronexus.quickstackcraft.compat.jei;

import dev.architectury.networking.NetworkManager;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.zeronexus.quickstackcraft.client.NearbyItemsCache;
import net.zeronexus.quickstackcraft.network.NearbyItemsScanC2SPacket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Shared logic for JEI transfer handlers (InventoryMenu and CraftingMenu).
 * Handles ingredient extraction, availability checking, and cache refresh.
 */
final class TransferHelper {

    private TransferHelper() {}

    /** Extract the 9 ingredient ItemStacks from JEI's recipe slot views. */
    static List<ItemStack> extractIngredients(IRecipeSlotsView recipeSlots) {
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

    /**
     * Check if the recipe ingredients are available from player inventory + nearby containers.
     * Triggers a server scan if the cache is stale.
     *
     * @return null if all ingredients available, or a COSMETIC/USER_FACING error
     */
    static IRecipeTransferError checkAvailability(List<ItemStack> ingredients, Player player) {
        // Trigger cache refresh if stale
        long tick = player.level().getGameTime();
        if (NearbyItemsCache.needsRefresh(tick)) {
            NearbyItemsCache.markQueried(tick);
            NetworkManager.sendToServer(new NearbyItemsScanC2SPacket());
        }

        // Build availability: player inventory + cached nearby container items
        Map<Item, Integer> available = new HashMap<>();

        // Player inventory (live, always accurate)
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                available.merge(stack.getItem(), stack.getCount(), Integer::sum);
            }
        }

        // Nearby containers (cached from last server scan)
        // Merge in cached counts
        NearbyItemsCache.forEachItem((item, count) ->
                available.merge(item, count, Integer::sum));

        // Check each ingredient against combined availability
        Map<Item, Integer> needed = new HashMap<>();
        for (ItemStack ingredient : ingredients) {
            if (!ingredient.isEmpty()) {
                needed.merge(ingredient.getItem(), 1, Integer::sum);
            }
        }

        boolean allAvailable = true;
        for (Map.Entry<Item, Integer> entry : needed.entrySet()) {
            if (available.getOrDefault(entry.getKey(), 0) < entry.getValue()) {
                allAvailable = false;
                break;
            }
        }

        if (allAvailable) {
            // All ingredients found - show green button with our tooltip
            return new CraftFromNearbyAvailable();
        } else {
            // Not enough ingredients anywhere - let JEI show default "missing items"
            return new CraftFromNearbyMissing();
        }
    }
}
