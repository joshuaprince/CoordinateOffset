package com.jtprince.coordinateoffset.offsetter.client;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientVehicleMove;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;

public class OffsetterClientVehicleMove extends PacketOffsetter<WrapperPlayClientVehicleMove> {
    public OffsetterClientVehicleMove() {
        super(WrapperPlayClientVehicleMove.class, PacketType.Play.Client.VEHICLE_MOVE);
    }

    @Override
    public void offset(WrapperPlayClientVehicleMove packet, Offset offset, User user) {
        packet.setPosition(unapply(packet.getPosition(), offset));
    }
}
