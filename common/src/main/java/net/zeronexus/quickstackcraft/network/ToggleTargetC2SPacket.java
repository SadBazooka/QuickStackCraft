package net.zeronexus.quickstackcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client -> Server: toggle the block the player is looking at in the storage whitelist
 * (or blacklist). The server raytraces the player's view to find the block, so the client
 * only needs to say which list to edit.
 *
 * @param blacklist true = toggle in the blacklist, false = toggle in the whitelist
 */
public record ToggleTargetC2SPacket(boolean blacklist) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ToggleTargetC2SPacket> TYPE =
            new CustomPacketPayload.Type<>(ModNetworking.id("toggle_target"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleTargetC2SPacket> CODEC =
            StreamCodec.of(
                    (buf, pkt) -> buf.writeBoolean(pkt.blacklist),
                    buf -> new ToggleTargetC2SPacket(buf.readBoolean()));

    @Override
    public Type<ToggleTargetC2SPacket> type() {
        return TYPE;
    }
}
