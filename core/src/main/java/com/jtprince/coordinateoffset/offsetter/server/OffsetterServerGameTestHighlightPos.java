package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerGameTestHighlightPos;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;

public class OffsetterServerGameTestHighlightPos extends PacketOffsetter<WrapperPlayServerGameTestHighlightPos> {
    public OffsetterServerGameTestHighlightPos() {
        super(WrapperPlayServerGameTestHighlightPos.class, PacketType.Play.Server.GAME_TEST_HIGHLIGHT_POS);
    }

    @Override
    public void offset(WrapperPlayServerGameTestHighlightPos packet, Offset offset, User user) {
        /* NB: Untested. Not clear how to trigger this packet. */
        packet.setAbsolutePos(apply(packet.getAbsolutePos(), offset));
    }
}
