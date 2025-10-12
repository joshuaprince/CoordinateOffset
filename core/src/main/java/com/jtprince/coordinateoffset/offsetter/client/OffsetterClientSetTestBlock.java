package com.jtprince.coordinateoffset.offsetter.client;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSetTestBlock;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;

public class OffsetterClientSetTestBlock extends PacketOffsetter<WrapperPlayClientSetTestBlock> {
    public OffsetterClientSetTestBlock() {
        super(WrapperPlayClientSetTestBlock.class, PacketType.Play.Client.SET_TEST_BLOCK);
    }

    @Override
    public void offset(WrapperPlayClientSetTestBlock packet, Offset offset, User user) {
        packet.setPosition(unapply(packet.getPosition(), offset));
    }
}
