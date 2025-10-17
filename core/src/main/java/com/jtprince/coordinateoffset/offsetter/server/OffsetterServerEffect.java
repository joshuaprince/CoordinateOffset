package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;
import com.jtprince.coordinateoffset.offsetter.wrapper.WrapperPlayServerEffect;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class OffsetterServerEffect extends PacketOffsetter<WrapperPlayServerEffect> {
    public OffsetterServerEffect() {
        super(WrapperPlayServerEffect.class, PacketType.Play.Server.EFFECT);
    }

    @Override
    public void offset(WrapperPlayServerEffect packet, Offset offset, User user) {
        packet.setPosition(apply(packet.getPosition(), offset));
    }
}
