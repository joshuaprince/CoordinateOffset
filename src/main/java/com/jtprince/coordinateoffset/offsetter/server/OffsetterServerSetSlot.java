package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;

public class OffsetterServerSetSlot extends PacketOffsetter<WrapperPlayServerSetSlot> {
    public OffsetterServerSetSlot() {
        super(WrapperPlayServerSetSlot.class, PacketType.Play.Server.SET_SLOT);
    }

    @Override
    public void offset(WrapperPlayServerSetSlot packet, Offset offset, User user) {
        packet.setItem(applyItemStack(packet.getItem(), offset));
    }
}
