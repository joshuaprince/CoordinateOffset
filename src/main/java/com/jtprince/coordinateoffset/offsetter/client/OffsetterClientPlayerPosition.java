package com.jtprince.coordinateoffset.offsetter.client;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;

public class OffsetterClientPlayerPosition extends PacketOffsetter<WrapperPlayClientPlayerFlying> {
    public OffsetterClientPlayerPosition() {
        super(WrapperPlayClientPlayerFlying.class,
                PacketType.Play.Client.PLAYER_POSITION, PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION);
    }

    @Override
    public void offset(WrapperPlayClientPlayerFlying packet, Offset offset, User user) {
        packet.setLocation(unapply(packet.getLocation(), offset));
    }
}
