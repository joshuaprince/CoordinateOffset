package com.jtprince.coordinateoffset.offsetter;

import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
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

    private static final BaseChunk[] emptyChunkList = {};

    public OffsettedColumn(Column column, Offset offset) {
        super(0, 0, false, emptyChunkList, null);
        this.inner = column;
        this.offset = offset;
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
        return inner.getTileEntities();
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
