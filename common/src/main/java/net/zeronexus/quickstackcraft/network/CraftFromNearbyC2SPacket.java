package net.zeronexus.quickstackcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Client -> Server: request to pull ingredients from nearby containers
 * into the player's crafting grid. Carries 9 ItemStack entries
 * representing the desired item in each crafting slot.
 */
public record CraftFromNearbyC2SPacket(List<ItemStack> ingredients) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<CraftFromNearbyC2SPacket> TYPE =
            new CustomPacketPayload.Type<>(ModNetworking.id("craft_nearby"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CraftFromNearbyC2SPacket> CODEC =
            StreamCodec.of(CraftFromNearbyC2SPacket::encode, CraftFromNearbyC2SPacket::decode);

    private static void encode(RegistryFriendlyByteBuf buf, CraftFromNearbyC2SPacket pkt) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = i < pkt.ingredients.size() ? pkt.ingredients.get(i) : ItemStack.EMPTY;
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, stack);
        }
    }

    private static CraftFromNearbyC2SPacket decode(RegistryFriendlyByteBuf buf) {
        List<ItemStack> items = new ArrayList<>(9);
        for (int i = 0; i < 9; i++) {
            items.add(ItemStack.OPTIONAL_STREAM_CODEC.decode(buf));
        }
        return new CraftFromNearbyC2SPacket(items);
    }

    @Override
    public Type<CraftFromNearbyC2SPacket> type() {
        return TYPE;
    }
}
