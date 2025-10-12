package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerAcknowledgePlayerDigging;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;

public class OffsetterServerAcknowledgePlayerDigging extends PacketOffsetter<WrapperPlayServerAcknowledgePlayerDigging> {
    public OffsetterServerAcknowledgePlayerDigging() {
        // Removed in 1.19 and replaced with ACKNOWLEDGE_BLOCK_CHANGES (which has no position)
        super(WrapperPlayServerAcknowledgePlayerDigging.class, PacketType.Play.Server.ACKNOWLEDGE_PLAYER_DIGGING);
    }

    @Override
    public void offset(WrapperPlayServerAcknowledgePlayerDigging packet, Offset offset, User user) {
        packet.setBlockPosition(apply(packet.getBlockPosition(), offset));
    }
}
