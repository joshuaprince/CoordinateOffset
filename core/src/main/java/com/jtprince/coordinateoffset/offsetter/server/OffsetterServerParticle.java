package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleItemStackData;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleTrailData;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleVibrationData;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.util.Vector3i;
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
            // startingPosition was only part of packet data up to 1.19.4. PE reports >1.19.4 with a zero vector.
            //  Make sure not to offset this zero vector, or it will leak offsets.
            if (!vibrationData.getStartingPosition().equals(Vector3i.zero())) {
                vibrationData.setStartingPosition(apply(vibrationData.getStartingPosition(), offset));
            }

            if (vibrationData.getBlockPosition().isPresent()) {
                vibrationData.setBlockPosition(apply(vibrationData.getBlockPosition().get(), offset));
            }
        }

        if (packet.getParticle().getData() instanceof ParticleItemStackData itemStackData) {
            ItemStack modifiedItemStack = applyItemStack(itemStackData.getItemStack(), offset);
            if (modifiedItemStack != null) {
                itemStackData.setItemStack(modifiedItemStack);
            }
        }

        if (packet.getParticle().getData() instanceof ParticleTrailData trailData) {
            trailData.setTarget(apply(trailData.getTarget(), offset));
        }
    }
}
