package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;

public class OffsetterServerEntityEquipment extends PacketOffsetter<WrapperPlayServerEntityEquipment> {
    public OffsetterServerEntityEquipment() {
        super(WrapperPlayServerEntityEquipment.class, PacketType.Play.Server.ENTITY_EQUIPMENT);
    }

    @Override
    public void offset(WrapperPlayServerEntityEquipment packet, Offset offset, User user) {
        for (Equipment equipment : packet.getEquipment()) {
            ItemStack modifiedItemStack = applyItemStack(equipment.getItem(), offset);
            if (modifiedItemStack != null) {
                equipment.setItem(modifiedItemStack);
            }
        }
    }
}
