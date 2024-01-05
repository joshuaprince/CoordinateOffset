package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleVibrationData;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerParticle;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;

public class OffsetterServerParticle extends PacketOffsetter<WrapperPlayServerParticle> {
    public OffsetterServerParticle() {
        super(WrapperPlayServerParticle.class, PacketType.Play.Server.PARTICLE);
    }

    @Override
    public void offset(WrapperPlayServerParticle packet, Offset offset, User user) {
        packet.setPosition(apply(packet.getPosition(), offset));

        if (packet.getParticle().getData() instanceof ParticleVibrationData vibrationData) {
            vibrationData.setStartingPosition(apply(vibrationData.getStartingPosition(), offset));
            if (vibrationData.getBlockPosition().isPresent()) {
                vibrationData.setBlockPosition(apply(vibrationData.getBlockPosition().get(), offset));
            }
        }
    }
}
