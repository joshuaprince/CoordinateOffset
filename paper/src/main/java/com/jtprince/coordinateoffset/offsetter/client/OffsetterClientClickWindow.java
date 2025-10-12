package com.jtprince.coordinateoffset.offsetter.client;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class OffsetterClientClickWindow extends PacketOffsetter<WrapperPlayClientClickWindow> {
    public OffsetterClientClickWindow() {
        super(WrapperPlayClientClickWindow.class, PacketType.Play.Client.CLICK_WINDOW);
    }

    @Override
    public void offset(WrapperPlayClientClickWindow packet, Offset offset, User user) {
        if (packet.getSlots().isPresent()) {
            Map<Integer, ItemStack> clientItems = packet.getSlots().get();
            Map<Integer, ItemStack> serverItems = clientItems.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, v -> {
                        ItemStack modifiedItem = unapplyItemStack(v.getValue(), offset);
                        if (modifiedItem != null) {
                            return modifiedItem;
                        } else {
                            return v.getValue();
                        }
                    }));
            packet.setSlots(Optional.of(serverItems));
        }

        /*
         * TODO: In 1.21.5, this packet was changed to contain a hashed ItemStack instead of the
         *  full ItemStack component data.
         *  More info: https://minecraft.wiki/w/Java_Edition_protocol/Slot_data#Hashed_Format
         *  It is impossible to get the ItemStack back from the hash, so we cannot simply unapply
         *  an offset here and make the offset fully transparent to the server.
         *  The result is that the server thinks the client has the wrong ItemStack. The server
         *  responds with a SetCursorItem packet with the correct ItemStack. This results in the
         *  client seeing a ghost item on the cursor.
         *  For now, the null check below ensures that this only affects items that need to be
         *  offsetted (i.e. lodestone compasses).
         *  The easiest way to reproduce this is to put a lodestone compass on the cursor and
         *  click-drag in the inventory window.
         */
        ItemStack modifiedCarriedItemStack = unapplyItemStack(packet.getCarriedItemStack(), offset);
        if (modifiedCarriedItemStack != null) {
            packet.setCarriedItemStack(modifiedCarriedItemStack);
        }
    }
}
