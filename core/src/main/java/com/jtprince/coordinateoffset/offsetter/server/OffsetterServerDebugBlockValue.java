package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDebugBlockValue;
import com.jtprince.coordinateoffset.FixedOffset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class OffsetterServerDebugBlockValue extends PacketOffsetter<WrapperPlayServerDebugBlockValue> {
    public OffsetterServerDebugBlockValue() {
        super(WrapperPlayServerDebugBlockValue.class, PacketType.Play.Server.DEBUG_BLOCK_VALUE);
    }

    @Override
    public void offset(WrapperPlayServerDebugBlockValue packet, FixedOffset offset, User user) {
        packet.setBlockPos(apply(packet.getBlockPos(), offset));
        // TODO: Look closer at fields like getUpdate() which probably can have entity locations in them.
        // Currently disabled entirely by default.
    }
}
