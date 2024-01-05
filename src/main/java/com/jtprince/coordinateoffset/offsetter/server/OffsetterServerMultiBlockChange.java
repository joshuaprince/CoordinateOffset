package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;

public class OffsetterServerMultiBlockChange extends PacketOffsetter<WrapperPlayServerMultiBlockChange> {
    public OffsetterServerMultiBlockChange() {
        super(WrapperPlayServerMultiBlockChange.class, PacketType.Play.Server.MULTI_BLOCK_CHANGE);
    }

    @Override
    public void offset(WrapperPlayServerMultiBlockChange packet, Offset offset, User user) {
        packet.setChunkPosition(applyChunk(packet.getChunkPosition(), offset));
    }
}
