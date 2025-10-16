package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDebugEvent;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;

public class OffsetterServerDebugEvent extends PacketOffsetter<WrapperPlayServerDebugEvent> {
    public OffsetterServerDebugEvent() {
        super(WrapperPlayServerDebugEvent.class, PacketType.Play.Server.DEBUG_EVENT);
    }

    @Override
    public void offset(WrapperPlayServerDebugEvent packet, Offset offset, User user) {
        // TODO: Drill into DebugSubscription.Event and offset any positions found there.
        // Currently disabled entirely by default.
    }
}
