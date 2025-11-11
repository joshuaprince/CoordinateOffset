package com.jtprince.coordinateoffset.offsetter.client;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSetStructureBlock;
import com.jtprince.coordinateoffset.FixedOffset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class OffsetterClientSetStructureBlock extends PacketOffsetter<WrapperPlayClientSetStructureBlock> {
    public OffsetterClientSetStructureBlock() {
        super(WrapperPlayClientSetStructureBlock.class, PacketType.Play.Client.UPDATE_STRUCTURE_BLOCK);
    }

    @Override
    public void offset(WrapperPlayClientSetStructureBlock packet, FixedOffset offset, User user) {
        packet.setPosition(unapply(packet.getPosition(), offset));
    }
}
