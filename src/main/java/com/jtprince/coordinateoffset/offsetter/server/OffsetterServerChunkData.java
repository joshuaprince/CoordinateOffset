package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChunkData;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;

public class OffsetterServerChunkData extends PacketOffsetter<WrapperPlayServerChunkData> {
    public OffsetterServerChunkData() {
        super(WrapperPlayServerChunkData.class, PacketType.Play.Server.CHUNK_DATA);
    }

    @Override
    public void offset(WrapperPlayServerChunkData packet, Offset offset, User user) {
        packet.setColumn(applyColumn(packet.getColumn(), offset, user));
    }
}
