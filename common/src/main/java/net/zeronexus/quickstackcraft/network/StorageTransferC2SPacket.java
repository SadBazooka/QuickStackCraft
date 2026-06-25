package net.zeronexus.quickstackcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client -> Server: quick stack or dump the contents of the container the player currently has
 * open (e.g. a Sophisticated Backpack) into nearby world storage.
 *
 * @param dumpAll true = Dump All (deposit everything), false = Quick Stack (only into
 *                containers that already hold a matching item)
 */
public record StorageTransferC2SPacket(boolean dumpAll) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<StorageTransferC2SPacket> TYPE =
            new CustomPacketPayload.Type<>(ModNetworking.id("storage_transfer"));

    public static final StreamCodec<RegistryFriendlyByteBuf, StorageTransferC2SPacket> CODEC =
            StreamCodec.of(
                    (buf, pkt) -> buf.writeBoolean(pkt.dumpAll),
                    buf -> new StorageTransferC2SPacket(buf.readBoolean()));

    @Override
    public Type<StorageTransferC2SPacket> type() {
        return TYPE;
    }
}
