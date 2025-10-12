package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;

public class OffsetterServerWindowItems extends PacketOffsetter<WrapperPlayServerWindowItems> {
    public OffsetterServerWindowItems() {
        super(WrapperPlayServerWindowItems.class, PacketType.Play.Server.WINDOW_ITEMS);
    }

    @Override
    public void offset(WrapperPlayServerWindowItems packet, Offset offset, User user) {
        packet.setItems(packet.getItems().stream().map(it -> {
            ItemStack modifiedItemStack = applyItemStack(it, offset);
            if (modifiedItemStack != null) {
                return modifiedItemStack;
            } else {
                return it;
            }
        }).toList());
        if (packet.getCarriedItem().isPresent()) {
            ItemStack modifiedItemStack = applyItemStack(packet.getCarriedItem().get(), offset);
            if (modifiedItemStack != null) {
                packet.setCarriedItem(modifiedItemStack);
            }
        }
    }
}
