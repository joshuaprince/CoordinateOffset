package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenSignEditor;
import com.jtprince.coordinateoffset.FixedOffset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class OffsetterServerOpenSignEditor extends PacketOffsetter<WrapperPlayServerOpenSignEditor> {
    public OffsetterServerOpenSignEditor() {
        super(WrapperPlayServerOpenSignEditor.class, PacketType.Play.Server.OPEN_SIGN_EDITOR);
    }

    @Override
    public void offset(WrapperPlayServerOpenSignEditor packet, FixedOffset offset, User user) {
        packet.setPosition(apply(packet.getPosition(), offset));
    }
}
