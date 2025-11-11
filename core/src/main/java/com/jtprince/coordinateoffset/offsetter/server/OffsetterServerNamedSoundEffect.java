package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.jtprince.coordinateoffset.FixedOffset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;
import com.jtprince.coordinateoffset.offsetter.wrapper.WrapperPlayServerNamedSoundEffect;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class OffsetterServerNamedSoundEffect extends PacketOffsetter<WrapperPlayServerNamedSoundEffect> {
    public OffsetterServerNamedSoundEffect() {
        // Removed around 1.19.2ish
        super(WrapperPlayServerNamedSoundEffect.class, PacketType.Play.Server.NAMED_SOUND_EFFECT);
    }

    @Override
    public void offset(WrapperPlayServerNamedSoundEffect packet, FixedOffset offset, User user) {
        packet.setEffectPosition(applyTimes8(packet.getEffectPosition(), offset));
    }
}
