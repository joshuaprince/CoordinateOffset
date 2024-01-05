package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.teleport.RelativeFlag;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerPositionAndLook;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;

public class OffsetterServerPlayerPositionAndLook extends PacketOffsetter<WrapperPlayServerPlayerPositionAndLook> {
    public OffsetterServerPlayerPositionAndLook() {
        super(WrapperPlayServerPlayerPositionAndLook.class, PacketType.Play.Server.PLAYER_POSITION_AND_LOOK);
    }

    @Override
    public void offset(WrapperPlayServerPlayerPositionAndLook packet, Offset offset, User user) {
        if (!packet.isRelativeFlag(RelativeFlag.X)) {
            packet.setX(applyX(packet.getX(), offset));
        }
        if (!packet.isRelativeFlag(RelativeFlag.Z)) {
            packet.setZ(applyZ(packet.getZ(), offset));
        }
    }
}
