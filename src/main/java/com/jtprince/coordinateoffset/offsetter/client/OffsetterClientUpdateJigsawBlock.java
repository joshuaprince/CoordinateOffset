package com.jtprince.coordinateoffset.offsetter.client;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientUpdateJigsawBlock;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;

public class OffsetterClientUpdateJigsawBlock extends PacketOffsetter<WrapperPlayClientUpdateJigsawBlock> {
    public OffsetterClientUpdateJigsawBlock() {
        super(WrapperPlayClientUpdateJigsawBlock.class, PacketType.Play.Client.UPDATE_JIGSAW_BLOCK);
    }

    @Override
    public void offset(WrapperPlayClientUpdateJigsawBlock packet, Offset offset, User user) {
        packet.setPosition(unapply(packet.getPosition(), offset));
    }
}
