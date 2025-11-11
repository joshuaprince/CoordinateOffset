package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPlayerInventory;
import com.jtprince.coordinateoffset.FixedOffset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class OffsetterServerSetPlayerInventory extends PacketOffsetter<WrapperPlayServerSetPlayerInventory> {
    public OffsetterServerSetPlayerInventory() {
        super(WrapperPlayServerSetPlayerInventory.class, PacketType.Play.Server.SET_PLAYER_INVENTORY);
    }

    @Override
    public void offset(WrapperPlayServerSetPlayerInventory packet, FixedOffset offset, User user) {
        ItemStack modifiedItemStack = applyItemStack(packet.getStack(), offset);
        if (modifiedItemStack != null) {
            packet.setStack(modifiedItemStack);
        }
    }
}
