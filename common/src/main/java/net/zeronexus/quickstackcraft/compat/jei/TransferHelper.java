package net.zeronexus.quickstackcraft.compat.jei;

import dev.architectury.networking.NetworkManager;
import mezz.jei.api.constants.VanillaTypes;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shared logic for JEI transfer handlers (InventoryMenu and CraftingMenu).
 * Handles ingredient extraction, availability checking, and cache refresh.
 *
 * <p>Each crafting input slot may accept many different items (e.g. "any plank", "any dye").
 * JEI cycles through these visually, but we extract <em>all</em> acceptable items per slot so
 * the [+] button works whenever the player owns ANY valid variant - not only the one JEI
 * happens to be displaying at that instant.
 */
final class TransferHelper {

    private TransferHelper() {}

    /** Hard cap on variants per slot, to keep the craft packet a sane size for huge tags. */
    static final int MAX_VARIANTS_PER_SLOT = 128;

    /**
     * Extract, for each crafting input slot, the full list of acceptable ItemStacks.
     * The returned list has one entry per input slot (in slot order); an empty inner list
     * means that slot is unused.
     */
    static List<List<ItemStack>> extractIngredientOptions(IRecipeSlotsView recipeSlots) {
        List<List<ItemStack>> perSlot = new ArrayList<>();
        recipeSlots.getSlotViews(RecipeIngredientRole.INPUT).forEach(slotView -> {
            List<ItemStack> options = new ArrayList<>();
            slotView.getIngredients(VanillaTypes.ITEM_STACK)
                    .filter(stack -> !stack.isEmpty())
                    .limit(MAX_VARIANTS_PER_SLOT)
                    .forEach(stack -> options.add(stack.copy()));
            perSlot.add(options);
        });
        return perSlot;
    }

    /**
     * Check whether the recipe can be satisfied from player inventory + nearby containers,
     * where each slot may be filled by ANY of its acceptable variants. Triggers a server
     * scan if the nearby-items cache is stale.
     *
     * @return null if all slots can be satisfied, or a COSMETIC/USER_FACING error
     */
    static IRecipeTransferError checkAvailability(List<List<ItemStack>> options, Player player) {
        // Trigger cache refresh if stale
        long tick = player.level().getGameTime();
        if (NearbyItemsCache.needsRefresh(tick)) {
            NearbyItemsCache.markQueried(tick);
            NetworkManager.sendToServer(new NearbyItemsScanC2SPacket());
        }

        // Build availability: player inventory + cached nearby container items
        Map<Item, Integer> available = new HashMap<>();

        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                available.merge(stack.getItem(), stack.getCount(), Integer::sum);
            }
        }
        NearbyItemsCache.forEachItem((item, count) -> available.merge(item, count, Integer::sum));

        boolean allAvailable = canSatisfy(options, available);
        return allAvailable ? new CraftFromNearbyAvailable() : new CraftFromNearbyMissing();
    }

    /**
     * Greedy multiset assignment: decide whether every used slot can be filled by some
     * available variant, accounting for items being consumed across slots. Slots with the
     * fewest options are assigned first (most-constrained-first) for a better packing.
     *
     * <p>Operates on a copy of {@code available} so the caller's map is untouched.
     */
    static boolean canSatisfy(List<List<ItemStack>> options, Map<Item, Integer> available) {
        Map<Item, Integer> pool = new HashMap<>(available);

        List<List<ItemStack>> used = new ArrayList<>();
        for (List<ItemStack> slot : options) {
            if (!slot.isEmpty()) used.add(slot);
        }
        used.sort(Comparator.comparingInt(List::size));

        for (List<ItemStack> slot : used) {
            boolean filled = false;
            for (ItemStack variant : slot) {
                Item item = variant.getItem();
                int have = pool.getOrDefault(item, 0);
                if (have > 0) {
                    pool.put(item, have - 1);
                    filled = true;
                    break;
                }
            }
            if (!filled) return false;
        }
        return true;
    }
}
