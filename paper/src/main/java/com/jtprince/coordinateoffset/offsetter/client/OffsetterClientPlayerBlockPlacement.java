package com.jtprince.coordinateoffset.offsetter.client;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;

public class OffsetterClientPlayerBlockPlacement extends PacketOffsetter<WrapperPlayClientPlayerBlockPlacement> {
    public OffsetterClientPlayerBlockPlacement() {
        super(WrapperPlayClientPlayerBlockPlacement.class, PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT);
    }

    @Override
    public void offset(WrapperPlayClientPlayerBlockPlacement packet, Offset offset, User user) {
        packet.setBlockPosition(unapply(packet.getBlockPosition(), offset));
    }
}
