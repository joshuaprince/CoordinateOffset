package com.jtprince.coordinateoffset.offsetter;

import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.world.chunk.BaseChunk;
import com.github.retrooper.packetevents.protocol.world.chunk.Column;
import com.github.retrooper.packetevents.protocol.world.chunk.TileEntity;
import com.jtprince.coordinateoffset.Offset;

/**
 * Wrapper for a PacketEvents Column (vertical slice of chunk sections) that returns offsetted
 * coordinates. (This was the cleanest way to wrap Column since it is not an interface)
 */
public class OffsettedColumn extends Column {
    private final Column inner;
    private final Offset offset;
    private final User user;

    private static final BaseChunk[] emptyChunkList = {};

    public OffsettedColumn(Column column, Offset offset, User user) {
        super(0, 0, false, emptyChunkList, null);
        this.inner = column;
        this.offset = offset;
        this.user = user;
    }

    public int getX() {
        return inner.getX() - offset.chunkX();
    }

    public int getZ() {
        return inner.getZ() - offset.chunkZ();
    }

    public boolean isFullChunk() {
        return inner.isFullChunk();
    }

    public BaseChunk[] getChunks() {
        return inner.getChunks();
    }

    public TileEntity[] getTileEntities() {
        // Tile entities are only absolutely positioned up to 1.17.1
        if (user.getClientVersion().isOlderThan(ClientVersion.V_1_18)) {
            TileEntity[] entities = inner.getTileEntities();
            for (TileEntity entity : entities) {
                entity.setX(entity.getX() - offset.x());
                entity.setZ(entity.getZ() - offset.z());
            }
            return entities;
        } else {
            return inner.getTileEntities();
        }
    }

    public boolean hasHeightMaps() {
        return inner.hasHeightMaps();
    }

    public NBTCompound getHeightMaps() {
        return inner.getHeightMaps();
    }

    public boolean hasBiomeData() {
        return inner.hasBiomeData();
    }

    public int[] getBiomeDataInts() {
        return inner.getBiomeDataInts();
    }

    public byte[] getBiomeDataBytes() {
        return inner.getBiomeDataBytes();
    }
}
