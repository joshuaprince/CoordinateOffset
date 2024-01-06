package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;
import com.jtprince.coordinateoffset.offsetter.wrapper.WrapperPlayServerSoundEffect_WithIdentifier;

public class OffsetterServerSoundEffect extends PacketOffsetter<WrapperPlayServerSoundEffect_WithIdentifier> {
    public OffsetterServerSoundEffect() {
        super(WrapperPlayServerSoundEffect_WithIdentifier.class, PacketType.Play.Server.SOUND_EFFECT);
    }

    @Override
    public void offset(WrapperPlayServerSoundEffect_WithIdentifier packet, Offset offset, User user) {
        packet.setEffectPosition(applyTimes8(packet.getEffectPosition(), offset));
    }
}
