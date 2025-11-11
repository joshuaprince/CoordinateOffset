package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnPosition;
import com.jtprince.coordinateoffset.FixedOffset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class OffsetterServerSpawnPosition extends PacketOffsetter<WrapperPlayServerSpawnPosition> {
    public OffsetterServerSpawnPosition() {
        super(WrapperPlayServerSpawnPosition.class, PacketType.Play.Server.SPAWN_POSITION);
    }

    @Override
    public void offset(WrapperPlayServerSpawnPosition packet, FixedOffset offset, User user) {
        packet.setPosition(apply(packet.getPosition(), offset));
    }
}
