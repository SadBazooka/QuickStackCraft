package net.zeronexus.quickstackcraft.network;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record QuickStackC2SPacket() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<QuickStackC2SPacket> TYPE =
            new CustomPacketPayload.Type<>(ModNetworking.id("quick_stack"));

    public static final StreamCodec<FriendlyByteBuf, QuickStackC2SPacket> CODEC =
            StreamCodec.unit(new QuickStackC2SPacket());

    @Override
    public Type<QuickStackC2SPacket> type() {
        return TYPE;
    }
}
