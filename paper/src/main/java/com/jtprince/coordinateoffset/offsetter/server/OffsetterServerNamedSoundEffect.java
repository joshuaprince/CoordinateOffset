package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;
import com.jtprince.coordinateoffset.offsetter.wrapper.WrapperPlayServerNamedSoundEffect;

public class OffsetterServerNamedSoundEffect extends PacketOffsetter<WrapperPlayServerNamedSoundEffect> {
    public OffsetterServerNamedSoundEffect() {
        // Removed around 1.19.2ish
        super(WrapperPlayServerNamedSoundEffect.class, PacketType.Play.Server.NAMED_SOUND_EFFECT);
    }

    @Override
    public void offset(WrapperPlayServerNamedSoundEffect packet, Offset offset, User user) {
        packet.setEffectPosition(applyTimes8(packet.getEffectPosition(), offset));
    }
}
