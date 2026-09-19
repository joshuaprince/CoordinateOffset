package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerAddTransientBlock;
import com.jtprince.coordinateoffset.FixedOffset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class OffsetterServerAddTransientBlock extends PacketOffsetter<WrapperPlayServerAddTransientBlock> {
    public OffsetterServerAddTransientBlock() {
        super(WrapperPlayServerAddTransientBlock.class, PacketType.Play.Server.ADD_TRANSIENT_BLOCK);
    }

    @Override
    public void offset(WrapperPlayServerAddTransientBlock packet, FixedOffset offset, User user) {
        // 26.3+: Used by falling blocks as they land.
        packet.setPosition(apply(packet.getPosition(), offset));
    }
}
