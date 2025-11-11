package com.jtprince.coordinateoffset.offsetter.client;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientUpdateCommandBlock;
import com.jtprince.coordinateoffset.FixedOffset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class OffsetterClientUpdateCommandBlock extends PacketOffsetter<WrapperPlayClientUpdateCommandBlock> {
    public OffsetterClientUpdateCommandBlock() {
        super(WrapperPlayClientUpdateCommandBlock.class, PacketType.Play.Client.UPDATE_COMMAND_BLOCK);
    }

    @Override
    public void offset(WrapperPlayClientUpdateCommandBlock packet, FixedOffset offset, User user) {
        packet.setPosition(unapply(packet.getPosition(), offset));
    }
}
