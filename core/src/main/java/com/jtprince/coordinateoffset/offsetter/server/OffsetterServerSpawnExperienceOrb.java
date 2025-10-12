package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnExperienceOrb;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;

public class OffsetterServerSpawnExperienceOrb extends PacketOffsetter<WrapperPlayServerSpawnExperienceOrb> {
    public OffsetterServerSpawnExperienceOrb() {
        super(WrapperPlayServerSpawnExperienceOrb.class, PacketType.Play.Server.SPAWN_EXPERIENCE_ORB);
    }

    @Override
    public void offset(WrapperPlayServerSpawnExperienceOrb packet, Offset offset, User user) {
        packet.setX(applyX(packet.getX(), offset));
        packet.setZ(applyZ(packet.getZ(), offset));
    }
}
