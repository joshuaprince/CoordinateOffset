package com.jtprince.coordinateoffset.offsetter.client;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientTestInstanceBlockAction;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;

public class OffsetterClientTestInstanceBlockAction extends PacketOffsetter<WrapperPlayClientTestInstanceBlockAction> {
    public OffsetterClientTestInstanceBlockAction() {
        super(WrapperPlayClientTestInstanceBlockAction.class, PacketType.Play.Client.TEST_INSTANCE_BLOCK_ACTION);
    }

    @Override
    public void offset(WrapperPlayClientTestInstanceBlockAction packet, Offset offset, User user) {
        packet.setPosition(unapply(packet.getPosition(), offset));
    }
}
