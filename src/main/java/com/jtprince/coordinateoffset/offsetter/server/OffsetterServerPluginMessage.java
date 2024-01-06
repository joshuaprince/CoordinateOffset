package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPluginMessage;
import com.jtprince.coordinateoffset.CoordinateOffset;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;

public class OffsetterServerPluginMessage extends PacketOffsetter<WrapperPlayServerPluginMessage> {
    public OffsetterServerPluginMessage() {
        super(WrapperPlayServerPluginMessage.class, PacketType.Play.Server.PLUGIN_MESSAGE);
    }

    @Override
    public void offset(WrapperPlayServerPluginMessage packet, Offset offset, User user) {
        CoordinateOffset.getInstance().getLogger().info(packet.getChannelName() + ": " + new String(packet.getData()));
    }
}
