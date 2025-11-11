package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDebugSample;
import com.jtprince.coordinateoffset.FixedOffset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class OffsetterServerDebugSample extends PacketOffsetter<WrapperPlayServerDebugSample> {
    public OffsetterServerDebugSample() {
        super(WrapperPlayServerDebugSample.class, PacketType.Play.Server.DEBUG_SAMPLE);
    }

    @Override
    public void offset(WrapperPlayServerDebugSample packet, FixedOffset offset, User user) {
        // TODO: Drill into Sample and offset any positions found there.
        // Currently disabled entirely by default.
    }
}
