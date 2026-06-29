package net.zeronexus.quickstackcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client -> Server: request a preview of where Quick Stack / Dump would deposit items. The server
 * scans nearby eligible storage and replies with a highlight packet (no items are moved).
 */
public record PreviewTargetsC2SPacket() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PreviewTargetsC2SPacket> TYPE =
            new CustomPacketPayload.Type<>(ModNetworking.id("preview_targets"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PreviewTargetsC2SPacket> CODEC =
            StreamCodec.of((buf, pkt) -> {}, buf -> new PreviewTargetsC2SPacket());

    @Override
    public Type<PreviewTargetsC2SPacket> type() {
        return TYPE;
    }
}
