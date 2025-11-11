package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDebugChunkValue;
import com.jtprince.coordinateoffset.FixedOffset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class OffsetterServerDebugChunkValue extends PacketOffsetter<WrapperPlayServerDebugChunkValue> {
    public OffsetterServerDebugChunkValue() {
        super(WrapperPlayServerDebugChunkValue.class, PacketType.Play.Server.DEBUG_CHUNK_VALUE);
    }

    @Override
    public void offset(WrapperPlayServerDebugChunkValue packet, FixedOffset offset, User user) {
        packet.setChunkPos(applyChunk(packet.getChunkPos(), offset));
        // TODO: Look closer at fields like getUpdate() which probably can have entity locations in them.
        // Currently disabled entirely by default.
    }
}
