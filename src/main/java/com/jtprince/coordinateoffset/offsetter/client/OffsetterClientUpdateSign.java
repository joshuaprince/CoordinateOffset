package com.jtprince.coordinateoffset.offsetter.client;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientUpdateSign;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;

public class OffsetterClientUpdateSign extends PacketOffsetter<WrapperPlayClientUpdateSign> {
    public OffsetterClientUpdateSign() {
        super(WrapperPlayClientUpdateSign.class, PacketType.Play.Client.UPDATE_SIGN);
    }

    @Override
    public void offset(WrapperPlayClientUpdateSign packet, Offset offset, User user) {
        packet.setBlockPosition(unapply(packet.getBlockPosition(), offset));
    }
}
