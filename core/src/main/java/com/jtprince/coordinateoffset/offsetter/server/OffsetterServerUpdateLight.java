package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateLight;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;

public class OffsetterServerUpdateLight extends PacketOffsetter<WrapperPlayServerUpdateLight> {
    public OffsetterServerUpdateLight() {
        super(WrapperPlayServerUpdateLight.class, PacketType.Play.Server.UPDATE_LIGHT);
    }

    @Override
    public void offset(WrapperPlayServerUpdateLight packet, Offset offset, User user) {
        packet.setX(applyChunkX(packet.getX(), offset));
        packet.setZ(applyChunkZ(packet.getZ(), offset));
    }
}
