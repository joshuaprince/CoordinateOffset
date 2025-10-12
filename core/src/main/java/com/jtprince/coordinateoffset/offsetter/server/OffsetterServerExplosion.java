package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerExplosion;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;

public class OffsetterServerExplosion extends PacketOffsetter<WrapperPlayServerExplosion> {
    public OffsetterServerExplosion() {
        super(WrapperPlayServerExplosion.class, PacketType.Play.Server.EXPLOSION);
    }

    @Override
    public void offset(WrapperPlayServerExplosion packet, Offset offset, User user) {
        packet.setPosition(apply(packet.getPosition(), offset));
        if (packet.getRecords() != null) { // Can be null >=1.21.2
            packet.setRecords(packet.getRecords().stream().map(v -> apply(v, offset)).toList());
        }
    }
}
