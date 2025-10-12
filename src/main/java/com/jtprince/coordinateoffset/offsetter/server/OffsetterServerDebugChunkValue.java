package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDebugChunkValue;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;

public class OffsetterServerDebugChunkValue extends PacketOffsetter<WrapperPlayServerDebugChunkValue> {
    public OffsetterServerDebugChunkValue() {
        super(WrapperPlayServerDebugChunkValue.class, PacketType.Play.Server.DEBUG_CHUNK_VALUE);
    }

    @Override
    public void offset(WrapperPlayServerDebugChunkValue packet, Offset offset, User user) {
        packet.setChunkPos(applyChunk(packet.getChunkPos(), offset));
    }
}
