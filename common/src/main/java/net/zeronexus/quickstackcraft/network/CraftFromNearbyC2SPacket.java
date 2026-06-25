package net.zeronexus.quickstackcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Client -> Server: request to pull ingredients from nearby containers into the player's
 * crafting grid.
 *
 * <p>Carries up to 9 crafting slots; each slot holds the list of acceptable item variants
 * for that position (e.g. every kind of plank a recipe accepts). The server fills each slot
 * with whichever variant the player actually owns, so the craft works regardless of which
 * variant JEI was displaying.
 */
public record CraftFromNearbyC2SPacket(List<List<ItemStack>> slotOptions) implements CustomPacketPayload {

    /** Defensive bound mirroring the client-side cap, so a malicious client can't send huge lists. */
    private static final int MAX_VARIANTS_PER_SLOT = 128;

    public static final CustomPacketPayload.Type<CraftFromNearbyC2SPacket> TYPE =
            new CustomPacketPayload.Type<>(ModNetworking.id("craft_nearby"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CraftFromNearbyC2SPacket> CODEC =
            StreamCodec.of(CraftFromNearbyC2SPacket::encode, CraftFromNearbyC2SPacket::decode);

    private static void encode(RegistryFriendlyByteBuf buf, CraftFromNearbyC2SPacket pkt) {
        for (int i = 0; i < 9; i++) {
            List<ItemStack> options = i < pkt.slotOptions.size() ? pkt.slotOptions.get(i) : List.of();
            int count = Math.min(options.size(), MAX_VARIANTS_PER_SLOT);
            buf.writeVarInt(count);
            for (int j = 0; j < count; j++) {
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, options.get(j));
            }
        }
    }

    private static CraftFromNearbyC2SPacket decode(RegistryFriendlyByteBuf buf) {
        List<List<ItemStack>> slots = new ArrayList<>(9);
        for (int i = 0; i < 9; i++) {
            int count = Math.min(buf.readVarInt(), MAX_VARIANTS_PER_SLOT);
            List<ItemStack> options = new ArrayList<>(count);
            for (int j = 0; j < count; j++) {
                ItemStack stack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
                if (!stack.isEmpty()) options.add(stack);
            }
            slots.add(options);
        }
        return new CraftFromNearbyC2SPacket(slots);
    }

    @Override
    public Type<CraftFromNearbyC2SPacket> type() {
        return TYPE;
    }
}
