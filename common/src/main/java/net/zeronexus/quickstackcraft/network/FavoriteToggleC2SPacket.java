package net.zeronexus.quickstackcraft.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record FavoriteToggleC2SPacket(int slot) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<FavoriteToggleC2SPacket> TYPE =
            new CustomPacketPayload.Type<>(ModNetworking.id("toggle_fav"));

    public static final StreamCodec<FriendlyByteBuf, FavoriteToggleC2SPacket> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, FavoriteToggleC2SPacket::slot,
                    FavoriteToggleC2SPacket::new
            );

    @Override
    public Type<FavoriteToggleC2SPacket> type() {
        return TYPE;
    }
}
