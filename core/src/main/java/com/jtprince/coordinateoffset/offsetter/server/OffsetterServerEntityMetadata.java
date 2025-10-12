package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;

import java.util.Optional;

public class OffsetterServerEntityMetadata extends PacketOffsetter<WrapperPlayServerEntityMetadata> {
    public OffsetterServerEntityMetadata() {
        super(WrapperPlayServerEntityMetadata.class, PacketType.Play.Server.ENTITY_METADATA);
    }

    @Override
    public void offset(WrapperPlayServerEntityMetadata packet, Offset offset, User user) {
        for (EntityData data : packet.getEntityMetadata()) {
            Object value = data.getValue();
            if (value == null) continue;
            if (value instanceof Optional<?> optional) {
                if (optional.isPresent()) {
                    data.setValue(Optional.of(applyOffsetToEntityMeta(optional.get(), offset)));
                }
            } else {
                data.setValue(applyOffsetToEntityMeta(value, offset));
            }
        }
    }

    private static Object applyOffsetToEntityMeta(Object object, Offset offset) {
        /*
         * Warning: Beware of adding Vector3d/Vector3f here as they are also used in display entity translation and
         * scale values (and probably other ones that should not be offsetted)
         */
        if (object instanceof Vector3i blockPosition) {
            return apply(blockPosition, offset);
        }
        if (object instanceof ItemStack) {
            ItemStack modifiedItemStack = applyItemStack((ItemStack) object, offset);
            if (modifiedItemStack != null) {
                return modifiedItemStack;
            } else {
                return object;
            }
        }
        return object;
    }
}
