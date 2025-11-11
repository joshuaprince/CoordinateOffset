package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerRespawn;
import com.jtprince.coordinateoffset.FixedOffset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class OffsetterServerRespawn extends PacketOffsetter<WrapperPlayServerRespawn> {
    public OffsetterServerRespawn() {
        super(WrapperPlayServerRespawn.class, PacketType.Play.Server.RESPAWN);
    }

    @Override
    public void offset(WrapperPlayServerRespawn packet, FixedOffset offset, User user) {
        if (packet.getLastDeathPosition() != null) {
            packet.setLastDeathPosition(apply(packet.getLastDeathPosition(), offset));
        }
    }
}
