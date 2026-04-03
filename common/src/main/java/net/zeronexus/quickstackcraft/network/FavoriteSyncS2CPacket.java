package net.zeronexus.quickstackcraft.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record FavoriteSyncS2CPacket(Set<Integer> slots) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<FavoriteSyncS2CPacket> TYPE =
            new CustomPacketPayload.Type<>(ModNetworking.id("sync_favs"));

    public static final StreamCodec<FriendlyByteBuf, FavoriteSyncS2CPacket> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()),
                    pkt -> List.copyOf(pkt.slots()),
                    list -> new FavoriteSyncS2CPacket(new HashSet<>(list))
            );

    @Override
    public Type<FavoriteSyncS2CPacket> type() {
        return TYPE;
    }
}
