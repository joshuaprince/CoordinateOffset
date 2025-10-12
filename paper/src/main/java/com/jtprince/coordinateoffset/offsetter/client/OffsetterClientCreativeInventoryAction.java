package com.jtprince.coordinateoffset.offsetter.client;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientCreativeInventoryAction;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;

public class OffsetterClientCreativeInventoryAction extends PacketOffsetter<WrapperPlayClientCreativeInventoryAction> {
    public OffsetterClientCreativeInventoryAction() {
        super(WrapperPlayClientCreativeInventoryAction.class, PacketType.Play.Client.CREATIVE_INVENTORY_ACTION);
    }

    @Override
    public void offset(WrapperPlayClientCreativeInventoryAction packet, Offset offset, User user) {
        ItemStack modifiedItemStack = unapplyItemStack(packet.getItemStack(), offset);
        if (modifiedItemStack != null) {
            packet.setItemStack(modifiedItemStack);
        }
    }
}
