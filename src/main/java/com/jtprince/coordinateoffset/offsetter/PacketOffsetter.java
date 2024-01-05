package com.jtprince.coordinateoffset.offsetter;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTInt;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.protocol.world.WorldBlockPosition;
import com.github.retrooper.packetevents.protocol.world.chunk.Column;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.jtprince.coordinateoffset.Offset;

public abstract class PacketOffsetter<T extends PacketWrapper<T>> {
    public final PacketTypeCommon[] packetTypes;
    public final Class<T> wrapperClass;

    public PacketOffsetter(Class<T> wrapperClass, PacketTypeCommon... packetTypes) {
        this.packetTypes = packetTypes;
        this.wrapperClass = wrapperClass;
    }

    public abstract void offset(T packet, Offset offset, User user);

    protected static Vector3d apply(Vector3d vec, Offset offset) {
        return new Vector3d(vec.x - offset.x(), vec.y, vec.z - offset.z());
    }

    protected static Vector3i apply(Vector3i vec, Offset offset) {
        return new Vector3i(vec.x - offset.x(), vec.y, vec.z - offset.z());
    }

    protected static Vector3d unapply(Vector3d vec, Offset offset) {
        return new Vector3d(vec.x + offset.x(), vec.y, vec.z + offset.z());
    }

    protected static Vector3i unapply(Vector3i vec, Offset offset) {
        return new Vector3i(vec.x + offset.x(), vec.y, vec.z + offset.z());
    }

    protected static Location unapply(Location loc, Offset offset) {
        loc.setPosition(new Vector3d(loc.getX() + offset.x(), loc.getY(), loc.getZ() + offset.z()));
        return loc;
    }

    protected static WorldBlockPosition apply(WorldBlockPosition pos, Offset offset) {
        // TODO: When available, this could respect the offset of the specific world instead of the Player's current one
        return new WorldBlockPosition(pos.getWorld(),
                pos.getBlockPosition().x - offset.x(), pos.getBlockPosition().y, pos.getBlockPosition().z - offset.z());
    }

    protected static Vector3i applyChunk(Vector3i vec, Offset offset) {
        return new Vector3i(vec.x - offset.chunkX(), vec.y, vec.z - offset.chunkZ());
    }

    protected static double applyX(double x, Offset offset) {
        return x - offset.x();
    }

    protected static double applyZ(double z, Offset offset) {
        return z - offset.z();
    }

    protected static int applyChunkX(int chunkX, Offset offset) {
        return chunkX - offset.chunkX();
    }

    protected static int applyChunkZ(int chunkZ, Offset offset) {
        return chunkZ - offset.chunkZ();
    }

    protected static OffsettedColumn applyColumn(Column column, Offset offset, User user) {
        if (column instanceof OffsettedColumn) return (OffsettedColumn) column;
        return new OffsettedColumn(column, offset, user);
    }

    protected static Vector3i applyTimes8(Vector3i vec, Offset offset) {
        // Used for sound effects
        return new Vector3i(vec.x - (offset.x() * 8), vec.y, vec.z - (offset.z() * 8));
    }

    protected static ItemStack applyItemStack(ItemStack item, Offset offset) {
        if (item == null) return null;

        if (item.getType() == ItemTypes.COMPASS) {
            NBTCompound nbt = item.getNBT();
            if (nbt != null) {
                NBTCompound lodestonePos = nbt.getCompoundTagOrNull("LodestonePos");
                if (lodestonePos != null) {
                    lodestonePos.setTag("X", new NBTInt(lodestonePos.getNumberTagOrThrow("X").getAsInt() - offset.x()));
                    lodestonePos.setTag("Z", new NBTInt(lodestonePos.getNumberTagOrThrow("Z").getAsInt() - offset.z()));
                }
            }
        }

        return item;
    }
}
