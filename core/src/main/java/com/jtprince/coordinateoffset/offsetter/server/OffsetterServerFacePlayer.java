package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerFacePlayer;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;

public class OffsetterServerFacePlayer extends PacketOffsetter<WrapperPlayServerFacePlayer> {
    public OffsetterServerFacePlayer() {
        super(WrapperPlayServerFacePlayer.class, PacketType.Play.Server.FACE_PLAYER);
    }

    @Override
    public void offset(WrapperPlayServerFacePlayer packet, Offset offset, User user) {
        packet.setTargetPosition(apply(packet.getTargetPosition(), offset));
    }
}
