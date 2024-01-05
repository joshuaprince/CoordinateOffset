package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateViewPosition;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;

public class OffsetterServerUpdateViewPosition extends PacketOffsetter<WrapperPlayServerUpdateViewPosition> {
    public OffsetterServerUpdateViewPosition() {
        super(WrapperPlayServerUpdateViewPosition.class, PacketType.Play.Server.UPDATE_VIEW_POSITION);
    }

    @Override
    public void offset(WrapperPlayServerUpdateViewPosition packet, Offset offset, User user) {
        packet.setChunkX(applyChunkX(packet.getChunkX(), offset));
        packet.setChunkZ(applyChunkZ(packet.getChunkZ(), offset));
    }
}
