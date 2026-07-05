package net.zeronexus.quickstackcraft.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client -> Server: dump the player's inventory into nearby storage.
 *
 * @param lockedMask bitmask of player-inventory slots (0-35) the client considers locked
 *                   (e.g. via ItemLocks); the server skips these in addition to favorited slots.
 */
public record DumpC2SPacket(long lockedMask) implements CustomPacketPayload {

    /** Convenience for callers with no external locks to report. */
    public DumpC2SPacket() { this(0L); }

    public static final CustomPacketPayload.Type<DumpC2SPacket> TYPE =
            new CustomPacketPayload.Type<>(ModNetworking.id("dump_all"));

    public static final StreamCodec<FriendlyByteBuf, DumpC2SPacket> CODEC =
            StreamCodec.of(
                    (buf, pkt) -> buf.writeLong(pkt.lockedMask),
                    buf -> new DumpC2SPacket(buf.readLong()));

    /** @return whether the given player-inventory slot was flagged locked by the client. */
    public boolean isLocked(int slot) {
        return slot >= 0 && slot < 64 && ((lockedMask >> slot) & 1L) != 0L;
    }

    @Override
    public Type<DumpC2SPacket> type() {
        return TYPE;
    }
}
