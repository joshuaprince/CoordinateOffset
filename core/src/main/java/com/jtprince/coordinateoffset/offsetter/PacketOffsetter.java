package com.jtprince.coordinateoffset.offsetter;

import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.component.builtin.item.LodestoneTracker;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTInt;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.protocol.world.WorldBlockPosition;
import com.github.retrooper.packetevents.protocol.world.chunk.Column;
import com.github.retrooper.packetevents.util.Vector2i;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.jtprince.coordinateoffset.CoordinateOffsetCore;
import com.jtprince.coordinateoffset.FixedOffset;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

@NullMarked
public abstract class PacketOffsetter<T extends PacketWrapper<T>> {
    public final PacketTypeCommon[] packetTypes;
    public final Class<T> wrapperClass;

    public PacketOffsetter(Class<T> wrapperClass, PacketTypeCommon... packetTypes) {
        this.packetTypes = packetTypes;
        this.wrapperClass = wrapperClass;
    }

    public abstract void offset(T packet, FixedOffset offset, User user);

    public void onUserDisconnect(User user) {}

    protected static Vector3d apply(Vector3d vec, FixedOffset offset) {
        return new Vector3d(vec.x - offset.x(), vec.y - offset.y(), vec.z - offset.z());
    }

    protected static Vector3f apply(Vector3f vec, FixedOffset offset) {
        return new Vector3f(vec.x - offset.x(), vec.y - offset.y(), vec.z - offset.z());
    }

    protected static Vector3i apply(Vector3i vec, FixedOffset offset) {
        return new Vector3i(vec.x - offset.x(), vec.y - offset.y(), vec.z - offset.z());
    }

    protected static Vector3d unapply(Vector3d vec, FixedOffset offset) {
        return new Vector3d(vec.x + offset.x(), vec.y + offset.y(), vec.z + offset.z());
    }

    protected static Vector3f unapply(Vector3f vec, FixedOffset offset) {
        return new Vector3f(vec.x + offset.x(), vec.y + offset.y(), vec.z + offset.z());
    }

    protected static Vector3i unapply(Vector3i vec, FixedOffset offset) {
        return new Vector3i(vec.x + offset.x(), vec.y + offset.y(), vec.z + offset.z());
    }

    protected static Location unapply(Location loc, FixedOffset offset) {
        loc.setPosition(new Vector3d(loc.getX() + offset.x(), loc.getY() + offset.y(), loc.getZ() + offset.z()));
        return loc;
    }

    protected static WorldBlockPosition apply(WorldBlockPosition pos, FixedOffset offset) {
        // TODO: When available, this could respect the offset of the specific world instead of the Player's current one
        return new WorldBlockPosition(pos.getWorld(),
                pos.getBlockPosition().x - offset.x(), pos.getBlockPosition().y - offset.y(), pos.getBlockPosition().z - offset.z());
    }

    protected static Vector3i applyChunk(Vector3i vec, FixedOffset offset) {
        return new Vector3i(vec.x - offset.chunkX(), vec.y, vec.z - offset.chunkZ());
    }

    protected static Vector2i applyChunk(Vector2i vec, FixedOffset offset) {
        return new Vector2i(vec.getX() - offset.chunkX(), vec.getZ() - offset.chunkZ());
    }

    protected static double applyX(double x, FixedOffset offset) {
        return x - offset.x();
    }

    protected static double applyZ(double z, FixedOffset offset) {
        return z - offset.z();
    }

    protected static int applyChunkX(int chunkX, FixedOffset offset) {
        return chunkX - offset.chunkX();
    }

    protected static int applyChunkZ(int chunkZ, FixedOffset offset) {
        return chunkZ - offset.chunkZ();
    }

    protected static OffsettedColumn applyColumn(Column column, FixedOffset offset, User user) {
        if (column instanceof OffsettedColumn) return (OffsettedColumn) column;
        return new OffsettedColumn(column, offset, user);
    }

    protected static Vector3i applyTimes8(Vector3i vec, FixedOffset offset) {
        // Used for sound effects
        return new Vector3i(vec.x - (offset.x() * 8), vec.y - (offset.y() * 8), vec.z - (offset.z() * 8));
    }

    /**
     * Apply an offset to all locational data components on an ItemStack.
     * @return The modified ItemStack, or null if no locational data components exist on the ItemStack.
     */
    protected static @Nullable ItemStack applyItemStack(ItemStack item, FixedOffset offset) {
        if (item == null) return null;

        if (item.getType() == ItemTypes.COMPASS) {
            // Up to 1.20.4 only: NBT tags
            NBTCompound nbt = item.getNBT();
            if (nbt != null) {
                NBTCompound lodestonePos = nbt.getCompoundTagOrNull("LodestonePos");
                if (lodestonePos != null) {
                    lodestonePos.setTag("X", new NBTInt(lodestonePos.getNumberTagOrThrow("X").getAsInt() - offset.x()));
                    lodestonePos.setTag("Z", new NBTInt(lodestonePos.getNumberTagOrThrow("Z").getAsInt() - offset.z()));
                    return item;
                }
            }

            // 1.20.5+ only: Components
            Optional<?> lodestoneComponent = null;
            try {
                lodestoneComponent = item.getComponents().getPatches().get(ComponentTypes.LODESTONE_TRACKER);
            } catch (NoSuchMethodError e) {
                // No error logged here because this Components branch only affects 1.20.5+, and PE will hit other
                //  issues if an outdated version is installed. (i.e. this branch only happens on <1.20.5 where it does
                //  not matter)
                CoordinateOffsetCore.get().getLogger().fine("Outdated PacketEvents! Failed to get item components.");
            }
            if (lodestoneComponent != null
                    && lodestoneComponent.isPresent()
                    && lodestoneComponent.get() instanceof LodestoneTracker lodestone
                    && lodestone.getTarget() != null) {
                lodestone.setTarget(apply(lodestone.getTarget(), offset));
                return item;
            }
        }

        return null;
    }

    /**
     * Unapply an offset to all locational data components on an ItemStack.
     * @return The modified ItemStack, or null if no locational data components exist on the ItemStack.
     */
    protected static @Nullable ItemStack unapplyItemStack(ItemStack item, FixedOffset offset) {
        return applyItemStack(item, offset.negate());
    }
}
