package com.jtprince.coordinateoffset.offsetter.client;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientTeleportConfirm;
import com.jtprince.coordinateoffset.FixedOffset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class OffsetterClientTeleportConfirm extends PacketOffsetter<WrapperPlayClientTeleportConfirm> {
    public OffsetterClientTeleportConfirm() {
        super(WrapperPlayClientTeleportConfirm.class, PacketType.Play.Client.TELEPORT_CONFIRM);
    }

    @Override
    public void offset(WrapperPlayClientTeleportConfirm packet, FixedOffset offset, User user) {
        packet.setX(unapplyX(packet.getX(), offset));
        packet.setZ(unapplyZ(packet.getZ(), offset));
    }
}
