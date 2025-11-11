package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerVehicleMove;
import com.jtprince.coordinateoffset.FixedOffset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class OffsetterServerVehicleMove extends PacketOffsetter<WrapperPlayServerVehicleMove> {
    public OffsetterServerVehicleMove() {
        super(WrapperPlayServerVehicleMove.class, PacketType.Play.Server.VEHICLE_MOVE);
    }

    @Override
    public void offset(WrapperPlayServerVehicleMove packet, FixedOffset offset, User user) {
        packet.setPosition(apply(packet.getPosition(), offset));
    }
}
