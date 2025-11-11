package com.jtprince.coordinateoffset.offsetter.client;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientTestInstanceBlockAction;
import com.jtprince.coordinateoffset.FixedOffset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class OffsetterClientTestInstanceBlockAction extends PacketOffsetter<WrapperPlayClientTestInstanceBlockAction> {
    public OffsetterClientTestInstanceBlockAction() {
        super(WrapperPlayClientTestInstanceBlockAction.class, PacketType.Play.Client.TEST_INSTANCE_BLOCK_ACTION);
    }

    @Override
    public void offset(WrapperPlayClientTestInstanceBlockAction packet, FixedOffset offset, User user) {
        packet.setPosition(unapply(packet.getPosition(), offset));
    }
}
