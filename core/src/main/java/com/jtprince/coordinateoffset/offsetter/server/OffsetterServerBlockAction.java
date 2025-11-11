package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockAction;
import com.jtprince.coordinateoffset.FixedOffset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class OffsetterServerBlockAction extends PacketOffsetter<WrapperPlayServerBlockAction> {
    public OffsetterServerBlockAction() {
        super(WrapperPlayServerBlockAction.class, PacketType.Play.Server.BLOCK_ACTION);
    }

    @Override
    public void offset(WrapperPlayServerBlockAction packet, FixedOffset offset, User user) {
        packet.setBlockPosition(apply(packet.getBlockPosition(), offset));
    }
}
