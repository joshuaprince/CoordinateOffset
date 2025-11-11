package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange;
import com.jtprince.coordinateoffset.FixedOffset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class OffsetterServerBlockChange extends PacketOffsetter<WrapperPlayServerBlockChange> {
    public OffsetterServerBlockChange() {
        super(WrapperPlayServerBlockChange.class, PacketType.Play.Server.BLOCK_CHANGE);
    }

    @Override
    public void offset(WrapperPlayServerBlockChange packet, FixedOffset offset, User user) {
        packet.setBlockPosition(apply(packet.getBlockPosition(), offset));
    }
}
