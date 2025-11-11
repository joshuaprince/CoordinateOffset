package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerGameTestHighlightPos;
import com.jtprince.coordinateoffset.FixedOffset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class OffsetterServerGameTestHighlightPos extends PacketOffsetter<WrapperPlayServerGameTestHighlightPos> {
    public OffsetterServerGameTestHighlightPos() {
        super(WrapperPlayServerGameTestHighlightPos.class, PacketType.Play.Server.GAME_TEST_HIGHLIGHT_POS);
    }

    @Override
    public void offset(WrapperPlayServerGameTestHighlightPos packet, FixedOffset offset, User user) {
        /* NB: Untested. Not clear how to trigger this packet. */
        packet.setAbsolutePos(apply(packet.getAbsolutePos(), offset));
    }
}
