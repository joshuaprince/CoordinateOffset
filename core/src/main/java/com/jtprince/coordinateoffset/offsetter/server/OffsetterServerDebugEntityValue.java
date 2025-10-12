package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDebugEntityValue;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;

public class OffsetterServerDebugEntityValue extends PacketOffsetter<WrapperPlayServerDebugEntityValue> {
    public OffsetterServerDebugEntityValue() {
        super(WrapperPlayServerDebugEntityValue.class, PacketType.Play.Server.DEBUG_ENTITY_VALUE);
    }

    @Override
    public void offset(WrapperPlayServerDebugEntityValue packet, Offset offset, User user) {
        // TODO: Drill into DebugSubscription.Update and offset any positions found there.
        // I'll let servers disable this packet entirely by default with any offset active until then.
    }
}
