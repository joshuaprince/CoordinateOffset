package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTInt;
import com.github.retrooper.packetevents.protocol.nbt.NBTNumber;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockEntityData;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;

public class OffsetterServerBlockEntityData extends PacketOffsetter<WrapperPlayServerBlockEntityData> {
    public OffsetterServerBlockEntityData() {
        super(WrapperPlayServerBlockEntityData.class, PacketType.Play.Server.BLOCK_ENTITY_DATA);
    }

    @Override
    public void offset(WrapperPlayServerBlockEntityData packet, Offset offset, User user) {
        packet.setPosition(apply(packet.getPosition(), offset));

        // TBD: I'm not sure which tile entity these are used for, but I'm keeping them from upstream just in case.
        if (packet.getNBT() != null) {
            NBTCompound nbt = packet.getNBT();
            NBTNumber x = nbt.getNumberTagOrNull("x");
            NBTNumber z = nbt.getNumberTagOrNull("z");
            if (x != null && z != null) {
                nbt.setTag("x", new NBTInt(x.getAsInt() - offset.x()));
                nbt.setTag("z", new NBTInt(z.getAsInt() - offset.z()));
            }
        }
    }
}
