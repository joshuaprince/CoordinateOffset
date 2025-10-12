package com.jtprince.coordinateoffset.offsetter.wrapper;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Server;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

@SuppressWarnings("unused") // Constructors are called reflectively
public class WrapperPlayServerEffect extends PacketWrapper<WrapperPlayServerEffect> {
    private int eventId;
    private Vector3i position;
    private byte[] remainingData; // Lazily ignoring everything after position, since only position matters for this plugin

    public WrapperPlayServerEffect(PacketSendEvent event) {
        super(event);
    }

    public WrapperPlayServerEffect(int eventId, Vector3i position, byte[] remainingData) {
        super(Server.EFFECT);
        this.eventId = eventId;
        this.position = position;
        this.remainingData = remainingData;
    }

    public void read() {
        this.eventId = this.readInt();
        this.position = new Vector3i(this.readLong());
        this.remainingData = this.readRemainingBytes();
    }

    public void write() {
        this.writeInt(this.eventId);
        long positionVector = this.position.getSerializedPosition();
        this.writeLong(positionVector);
        this.writeBytes(this.remainingData);
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public Vector3i getPosition() {
        return position;
    }

    public void setPosition(Vector3i position) {
        this.position = position;
    }
}
