package com.jtprince.coordinateoffset.offsetter.client;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;

public class OffsetterClientPlayerDigging extends PacketOffsetter<WrapperPlayClientPlayerDigging> {
    public OffsetterClientPlayerDigging() {
        super(WrapperPlayClientPlayerDigging.class, PacketType.Play.Client.PLAYER_DIGGING);
    }

    @Override
    public void offset(WrapperPlayClientPlayerDigging packet, Offset offset, User user) {
        packet.setBlockPosition(unapply(packet.getBlockPosition(), offset));
    }
}
