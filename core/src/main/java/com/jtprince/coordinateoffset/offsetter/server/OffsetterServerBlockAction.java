package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockAction;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;

public class OffsetterServerBlockAction extends PacketOffsetter<WrapperPlayServerBlockAction> {
    public OffsetterServerBlockAction() {
        super(WrapperPlayServerBlockAction.class, PacketType.Play.Server.BLOCK_ACTION);
    }

    @Override
    public void offset(WrapperPlayServerBlockAction packet, Offset offset, User user) {
        packet.setBlockPosition(apply(packet.getBlockPosition(), offset));
    }
}
