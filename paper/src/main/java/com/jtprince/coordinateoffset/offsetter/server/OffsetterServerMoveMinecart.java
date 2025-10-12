package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMoveMinecart;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;

public class OffsetterServerMoveMinecart extends PacketOffsetter<WrapperPlayServerMoveMinecart> {
    public OffsetterServerMoveMinecart() {
        super(WrapperPlayServerMoveMinecart.class, PacketType.Play.Server.MOVE_MINECART);
    }

    @Override
    public void offset(WrapperPlayServerMoveMinecart packet, Offset offset, User user) {
        // Note: As of 1.21.3, this packet is only used when the experimental minecart_improvements datapack is applied
        for (WrapperPlayServerMoveMinecart.MinecartStep step : packet.getLerpSteps()) {
            step.setPosition(apply(step.getPosition(), offset));
        }
    }
}
