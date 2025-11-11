package com.jtprince.coordinateoffset.offsetter.client;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientVehicleMove;
import com.jtprince.coordinateoffset.FixedOffset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class OffsetterClientVehicleMove extends PacketOffsetter<WrapperPlayClientVehicleMove> {
    public OffsetterClientVehicleMove() {
        super(WrapperPlayClientVehicleMove.class, PacketType.Play.Client.VEHICLE_MOVE);
    }

    @Override
    public void offset(WrapperPlayClientVehicleMove packet, FixedOffset offset, User user) {
        packet.setPosition(unapply(packet.getPosition(), offset));
    }
}
