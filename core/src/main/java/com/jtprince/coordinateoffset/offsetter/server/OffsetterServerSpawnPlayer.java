package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnPlayer;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;

public class OffsetterServerSpawnPlayer extends PacketOffsetter<WrapperPlayServerSpawnPlayer> {
    public OffsetterServerSpawnPlayer() {
        // Removed in 1.19.3
        super(WrapperPlayServerSpawnPlayer.class, PacketType.Play.Server.SPAWN_PLAYER);
    }

    @Override
    public void offset(WrapperPlayServerSpawnPlayer packet, Offset offset, User user) {
        packet.setPosition(apply(packet.getPosition(), offset));
    }
}
