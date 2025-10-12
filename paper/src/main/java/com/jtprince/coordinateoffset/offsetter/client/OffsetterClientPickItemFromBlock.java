package com.jtprince.coordinateoffset.offsetter.client;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPickItemFromBlock;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;

public class OffsetterClientPickItemFromBlock extends PacketOffsetter<WrapperPlayClientPickItemFromBlock> {
    public OffsetterClientPickItemFromBlock() {
        super(WrapperPlayClientPickItemFromBlock.class, PacketType.Play.Client.PICK_ITEM_FROM_BLOCK);
    }

    @Override
    public void offset(WrapperPlayClientPickItemFromBlock packet, Offset offset, User user) {
        packet.setBlockPos(unapply(packet.getBlockPos(), offset));
    }
}
