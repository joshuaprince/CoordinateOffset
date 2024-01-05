package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;

public class OffsetterServerEntityTeleport extends PacketOffsetter<WrapperPlayServerEntityTeleport> {
    public OffsetterServerEntityTeleport() {
        super(WrapperPlayServerEntityTeleport.class, PacketType.Play.Server.ENTITY_TELEPORT);
    }

    @Override
    public void offset(WrapperPlayServerEntityTeleport packet, Offset offset, User user) {
        packet.setPosition(apply(packet.getPosition(), offset));
    }
}
