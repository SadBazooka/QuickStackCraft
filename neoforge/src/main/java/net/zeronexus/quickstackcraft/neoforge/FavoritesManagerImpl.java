package net.zeronexus.quickstackcraft.neoforge;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.zeronexus.quickstackcraft.QuickStackCraft;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;

/**
 * NeoForge implementation of FavoritesManager using player attachments.
 * The @ExpectPlatform in common/FavoritesManager delegates to these static methods.
 * Method signatures must match the common @ExpectPlatform methods exactly.
 */
public final class FavoritesManagerImpl {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, QuickStackCraft.MOD_ID);

    @SuppressWarnings("unchecked")
    public static final Supplier<AttachmentType<Set<Integer>>> FAVORITE_SLOTS = ATTACHMENTS.register(
            "favorite_slots",
            () -> AttachmentType.<Set<Integer>>builder(() -> new HashSet<>())
                    .serialize(Codec.INT.listOf().xmap(
                            list -> (Set<Integer>) new HashSet<>(list),
                            set -> set.stream().sorted().toList()
                    ))
                    .copyOnDeath()
                    .build()
    );

    private FavoritesManagerImpl() {}

    public static boolean isFavorited(Player player, int slot) {
        return player.getData(FAVORITE_SLOTS.get()).contains(slot);
    }

    public static void toggle(Player player, int slot) {
        Set<Integer> slots = new HashSet<>(player.getData(FAVORITE_SLOTS.get()));
        if (slots.contains(slot)) {
            slots.remove(slot);
        } else {
            slots.add(slot);
        }
        player.setData(FAVORITE_SLOTS.get(), slots);
    }

    public static Set<Integer> getAll(Player player) {
        return Set.copyOf(player.getData(FAVORITE_SLOTS.get()));
    }

    public static void setAll(Player player, Set<Integer> slots) {
        player.setData(FAVORITE_SLOTS.get(), new HashSet<>(slots));
    }
}
