package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;

public class OffsetterServerSpawnEntity extends PacketOffsetter<WrapperPlayServerSpawnEntity> {
    public OffsetterServerSpawnEntity() {
        super(WrapperPlayServerSpawnEntity.class, PacketType.Play.Server.SPAWN_ENTITY);
    }

    @Override
    public void offset(WrapperPlayServerSpawnEntity packet, Offset offset, User user) {
        packet.setPosition(apply(packet.getPosition(), offset));
    }
}
