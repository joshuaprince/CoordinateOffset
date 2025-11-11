package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetCursorItem;
import com.jtprince.coordinateoffset.FixedOffset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class OffsetterServerSetCursorItem extends PacketOffsetter<WrapperPlayServerSetCursorItem> {
    public OffsetterServerSetCursorItem() {
        super(WrapperPlayServerSetCursorItem.class, PacketType.Play.Server.SET_CURSOR_ITEM);
    }

    @Override
    public void offset(WrapperPlayServerSetCursorItem packet, FixedOffset offset, User user) {
        ItemStack modifiedItemStack = applyItemStack(packet.getStack(), offset);
        if (modifiedItemStack != null) {
            packet.setStack(modifiedItemStack);
        }
    }
}
