package com.jtprince.coordinateoffset.offsetter.wrapper;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Server;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

@SuppressWarnings("unused") // Constructors are called reflectively
public class WrapperPlayServerUpdateLight extends PacketWrapper<WrapperPlayServerUpdateLight> {
    private int chunkX;
    private int chunkZ;
    private byte[] remainingData; // Lazily ignoring everything after position, since only position matters for this plugin

    public WrapperPlayServerUpdateLight(PacketSendEvent event) {
        super(event);
    }

    public WrapperPlayServerUpdateLight(int chunkX, int chunkZ, byte[] remainingData) {
        super(Server.UPDATE_LIGHT);
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.remainingData = remainingData;
    }

    public void read() {
        this.chunkX = this.readVarInt();
        this.chunkZ = this.readVarInt();
        this.remainingData = this.readRemainingBytes();
    }

    public void write() {
        this.writeVarInt(this.chunkX);
        this.writeVarInt(this.chunkZ);
        this.writeBytes(this.remainingData);
    }

    public int getChunkX() {
        return chunkX;
    }

    public void setChunkX(int chunkX) {
        this.chunkX = chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public void setChunkZ(int chunkZ) {
        this.chunkZ = chunkZ;
    }
}
