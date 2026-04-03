package net.zeronexus.quickstackcraft.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record DumpC2SPacket() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<DumpC2SPacket> TYPE =
            new CustomPacketPayload.Type<>(ModNetworking.id("dump_all"));

    public static final StreamCodec<FriendlyByteBuf, DumpC2SPacket> CODEC =
            StreamCodec.unit(new DumpC2SPacket());

    @Override
    public Type<DumpC2SPacket> type() {
        return TYPE;
    }
}
