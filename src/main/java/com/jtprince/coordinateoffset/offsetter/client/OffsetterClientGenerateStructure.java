package com.jtprince.coordinateoffset.offsetter.client;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientGenerateStructure;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;

public class OffsetterClientGenerateStructure extends PacketOffsetter<WrapperPlayClientGenerateStructure> {
    public OffsetterClientGenerateStructure() {
        super(WrapperPlayClientGenerateStructure.class, PacketType.Play.Client.GENERATE_STRUCTURE);
    }

    @Override
    public void offset(WrapperPlayClientGenerateStructure packet, Offset offset, User user) {
        packet.setBlockPosition(unapply(packet.getBlockPosition(), offset));
    }
}
