package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;
import com.jtprince.coordinateoffset.offsetter.wrapper.WrapperPlayServerSculkVibrationSignal;

public class OffsetterServerSculkVibrationSignal extends PacketOffsetter<WrapperPlayServerSculkVibrationSignal> {
    public OffsetterServerSculkVibrationSignal() {
        // Removed in 1.19
        super(WrapperPlayServerSculkVibrationSignal.class, PacketType.Play.Server.SCULK_VIBRATION_SIGNAL);
    }

    @Override
    public void offset(WrapperPlayServerSculkVibrationSignal packet, Offset offset, User user) {
        packet.setSourcePosition(apply(packet.getSourcePosition(), offset));
        if (packet.getDestinationPosition() != null) {
            packet.setDestinationPosition(apply(packet.getDestinationPosition(), offset));
        }
    }
}
