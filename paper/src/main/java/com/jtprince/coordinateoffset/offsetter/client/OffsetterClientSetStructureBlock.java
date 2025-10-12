package com.jtprince.coordinateoffset.offsetter.client;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSetStructureBlock;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;

public class OffsetterClientSetStructureBlock extends PacketOffsetter<WrapperPlayClientSetStructureBlock> {
    public OffsetterClientSetStructureBlock() {
        super(WrapperPlayClientSetStructureBlock.class, PacketType.Play.Client.UPDATE_STRUCTURE_BLOCK);
    }

    @Override
    public void offset(WrapperPlayClientSetStructureBlock packet, Offset offset, User user) {
        packet.setPosition(unapply(packet.getPosition(), offset));
    }
}
