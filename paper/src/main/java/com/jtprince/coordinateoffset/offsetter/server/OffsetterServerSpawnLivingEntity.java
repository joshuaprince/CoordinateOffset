package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnLivingEntity;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;

public class OffsetterServerSpawnLivingEntity extends PacketOffsetter<WrapperPlayServerSpawnLivingEntity> {
    public OffsetterServerSpawnLivingEntity() {
        // Removed in 1.19
        super(WrapperPlayServerSpawnLivingEntity.class, PacketType.Play.Server.SPAWN_LIVING_ENTITY);
    }

    @Override
    public void offset(WrapperPlayServerSpawnLivingEntity packet, Offset offset, User user) {
        packet.setPosition(apply(packet.getPosition(), offset));
    }
}
