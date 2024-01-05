package com.jtprince.coordinateoffset.offsetter.wrapper;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Server;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

@SuppressWarnings("unused") // Constructors are called reflectively
public class WrapperPlayServerEffect extends PacketWrapper<WrapperPlayServerEffect> {
    private int eventId;
    private Vector3d position;
    private byte[] remainingData; // Lazily ignoring everything after position, since only position matters for this plugin

    public WrapperPlayServerEffect(PacketSendEvent event) {
        super(event);
    }

    public WrapperPlayServerEffect(int eventId, Vector3d position, byte[] remainingData) {
        super(Server.EFFECT);
        this.eventId = eventId;
        this.position = position;
        this.remainingData = remainingData;
    }

    public void read() {
        this.eventId = this.readInt();
        this.position = new Vector3d(this.readFloat(), this.readFloat(), this.readFloat());
        this.remainingData = this.readRemainingBytes();
    }

    public void write() {
        this.writeInt(this.eventId);
        this.writeFloat((float) this.position.getX());
        this.writeFloat((float) this.position.getY());
        this.writeFloat((float) this.position.getZ());
        this.writeBytes(this.remainingData);
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public Vector3d getPosition() {
        return position;
    }

    public void setPosition(Vector3d position) {
        this.position = position;
    }
}
