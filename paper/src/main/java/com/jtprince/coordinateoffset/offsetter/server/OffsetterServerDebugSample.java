package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDebugSample;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;

public class OffsetterServerDebugSample extends PacketOffsetter<WrapperPlayServerDebugSample> {
    public OffsetterServerDebugSample() {
        super(WrapperPlayServerDebugSample.class, PacketType.Play.Server.DEBUG_SAMPLE);
    }

    @Override
    public void offset(WrapperPlayServerDebugSample packet, Offset offset, User user) {
        // TODO: Drill into Sample and offset any positions found there.
        // I'll let servers disable this packet entirely by default with any offset active until then.
    }
}
