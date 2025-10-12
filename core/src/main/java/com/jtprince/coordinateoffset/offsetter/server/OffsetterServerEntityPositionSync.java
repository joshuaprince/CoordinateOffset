package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityPositionSync;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;

public class OffsetterServerEntityPositionSync extends PacketOffsetter<WrapperPlayServerEntityPositionSync> {
    public OffsetterServerEntityPositionSync() {
        super(WrapperPlayServerEntityPositionSync.class, PacketType.Play.Server.ENTITY_POSITION_SYNC);
    }

    @Override
    public void offset(WrapperPlayServerEntityPositionSync packet, Offset offset, User user) {
        packet.getValues().setPosition(apply(packet.getValues().getPosition(), offset));
    }
}
