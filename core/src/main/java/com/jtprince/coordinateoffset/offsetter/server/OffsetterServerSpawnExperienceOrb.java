package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnExperienceOrb;
import com.jtprince.coordinateoffset.FixedOffset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class OffsetterServerSpawnExperienceOrb extends PacketOffsetter<WrapperPlayServerSpawnExperienceOrb> {
    public OffsetterServerSpawnExperienceOrb() {
        super(WrapperPlayServerSpawnExperienceOrb.class, PacketType.Play.Server.SPAWN_EXPERIENCE_ORB);
    }

    @Override
    public void offset(WrapperPlayServerSpawnExperienceOrb packet, FixedOffset offset, User user) {
        packet.setX(applyX(packet.getX(), offset));
        packet.setZ(applyZ(packet.getZ(), offset));
    }
}
