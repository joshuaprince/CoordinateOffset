package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnPainting;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;

public class OffsetterServerSpawnPainting extends PacketOffsetter<WrapperPlayServerSpawnPainting> {
    public OffsetterServerSpawnPainting() {
        // Removed in 1.19
        super(WrapperPlayServerSpawnPainting.class, PacketType.Play.Server.SPAWN_PAINTING);
    }

    @Override
    public void offset(WrapperPlayServerSpawnPainting packet, Offset offset, User user) {
        packet.setPosition(apply(packet.getPosition(), offset));
    }
}
