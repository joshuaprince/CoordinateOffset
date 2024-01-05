package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSoundEffect;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;

public class OffsetterServerNamedSoundEffect extends PacketOffsetter<WrapperPlayServerSoundEffect> {
    public OffsetterServerNamedSoundEffect() {
        super(WrapperPlayServerSoundEffect.class, PacketType.Play.Server.NAMED_SOUND_EFFECT);
    }

    @Override
    public void offset(WrapperPlayServerSoundEffect packet, Offset offset) {
        packet.setEffectPosition(applyTimes8(packet.getEffectPosition(), offset));
    }
}
