package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerJoinGame;
import com.jtprince.coordinateoffset.FixedOffset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class OffsetterServerJoinGame extends PacketOffsetter<WrapperPlayServerJoinGame> {
    public OffsetterServerJoinGame() {
        super(WrapperPlayServerJoinGame.class, PacketType.Play.Server.JOIN_GAME);
    }

    @Override
    public void offset(WrapperPlayServerJoinGame packet, FixedOffset offset, User user) {
        if (packet.getLastDeathPosition() != null) {
            packet.setLastDeathPosition(apply(packet.getLastDeathPosition(), offset));
        }
    }
}
