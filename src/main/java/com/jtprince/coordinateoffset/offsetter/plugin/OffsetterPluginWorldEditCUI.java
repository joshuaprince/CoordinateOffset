package com.jtprince.coordinateoffset.offsetter.plugin;

import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPluginMessage;
import com.jtprince.coordinateoffset.Offset;

public class OffsetterPluginWorldEditCUI {
    public static void offset(WrapperPlayServerPluginMessage packet, Offset offset, User user) {
        // worldedit:cui channel name
        // references:
        // https://wiki.vg/Plugin_channels/WorldEditCUI
        // https://github.com/Mumfrey/WorldEditCUI/blob/master/java/com/mumfrey/worldeditcui/event/CUIEventType.java
    }
}
