package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;
import com.jtprince.coordinateoffset.offsetter.wrapper.WrapperPlayServerUpdateLight;

public class OffsetterServerUpdateLight extends PacketOffsetter<WrapperPlayServerUpdateLight> {
    public OffsetterServerUpdateLight() {
        super(WrapperPlayServerUpdateLight.class, PacketType.Play.Server.UPDATE_LIGHT);
    }

    @Override
    public void offset(WrapperPlayServerUpdateLight packet, Offset offset, User user) {
        packet.setChunkX(applyChunkX(packet.getChunkX(), offset));
        packet.setChunkZ(applyChunkZ(packet.getChunkZ(), offset));
    }
}
