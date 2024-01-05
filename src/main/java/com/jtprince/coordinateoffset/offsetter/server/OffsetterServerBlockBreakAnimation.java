package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockBreakAnimation;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;

public class OffsetterServerBlockBreakAnimation extends PacketOffsetter<WrapperPlayServerBlockBreakAnimation> {
    public OffsetterServerBlockBreakAnimation() {
        super(WrapperPlayServerBlockBreakAnimation.class, PacketType.Play.Server.BLOCK_BREAK_ANIMATION);
    }

    @Override
    public void offset(WrapperPlayServerBlockBreakAnimation packet, Offset offset, User user) {
        packet.setBlockPosition(apply(packet.getBlockPosition(), offset));
    }
}
