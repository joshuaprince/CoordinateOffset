package com.jtprince.coordinateoffset.offsetter.wrapper;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Server;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleVibrationData;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

import javax.annotation.Nullable;

// Removed in 1.19

@SuppressWarnings("unused") // Constructors are called reflectively
public class WrapperPlayServerSculkVibrationSignal extends PacketWrapper<WrapperPlayServerSculkVibrationSignal> {
    private Vector3i sourcePosition;
    private ResourceLocation destinationIdentifier;
    private @Nullable Vector3i destinationPosition; // Null if the destination is an entity ID
    private byte[] remainingData; // Lazily ignoring everything after position, since only position matters for this plugin

    public WrapperPlayServerSculkVibrationSignal(PacketSendEvent event) {
        super(event);
    }

    public WrapperPlayServerSculkVibrationSignal(Vector3i position, ResourceLocation destinationIdentifier, byte[] remainingData) {
        super(Server.SCULK_VIBRATION_SIGNAL);
        this.sourcePosition = position;
        this.destinationIdentifier = destinationIdentifier;
        this.remainingData = remainingData;
    }

    public void read() {
        this.sourcePosition = new Vector3i(this.readLong());
        this.destinationIdentifier = this.readIdentifier();
        if (destinationIdentifier.equals(ParticleVibrationData.PositionType.BLOCK.getName())) {
            this.destinationPosition = this.readBlockPosition();
        } else {
            this.destinationPosition = null;
        }
        this.remainingData = this.readRemainingBytes();
    }

    public void write() {
        long sourcePosition = this.sourcePosition.getSerializedPosition();
        this.writeLong(sourcePosition);
        this.writeIdentifier(this.destinationIdentifier);
        if (this.destinationPosition != null) {
            this.writeBlockPosition(this.destinationPosition);
        }
        this.writeBytes(this.remainingData);
    }

    public Vector3i getSourcePosition() {
        return sourcePosition;
    }

    public void setSourcePosition(Vector3i sourcePosition) {
        this.sourcePosition = sourcePosition;
    }

    public @Nullable Vector3i getDestinationPosition() {
        return destinationPosition;
    }

    public void setDestinationPosition(@Nullable Vector3i destinationPosition) {
        this.destinationPosition = destinationPosition;
    }
}
