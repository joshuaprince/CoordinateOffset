package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSoundEffect;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;

public class OffsetterServerSoundEffect extends PacketOffsetter<WrapperPlayServerSoundEffect> {
    public OffsetterServerSoundEffect() {
        super(WrapperPlayServerSoundEffect.class, PacketType.Play.Server.SOUND_EFFECT);
    }

    @Override
    public void offset(WrapperPlayServerSoundEffect packet, Offset offset, User user) {
        packet.setEffectPosition(applyTimes8(packet.getEffectPosition(), offset));
    }
}
