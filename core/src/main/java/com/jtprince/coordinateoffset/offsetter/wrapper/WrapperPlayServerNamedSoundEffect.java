package com.jtprince.coordinateoffset.offsetter.wrapper;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Server;
import com.github.retrooper.packetevents.protocol.sound.SoundCategory;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

@SuppressWarnings("unused") // Constructors are called reflectively
public class WrapperPlayServerNamedSoundEffect extends PacketWrapper<WrapperPlayServerNamedSoundEffect> {
    private ResourceLocation soundName;
    private SoundCategory soundCategory;
    private Vector3i effectPosition;
    private byte[] remainingData; // Lazily ignoring everything after position, since only position matters for this plugin

    public WrapperPlayServerNamedSoundEffect(PacketSendEvent event) {
        super(event);
    }

    public WrapperPlayServerNamedSoundEffect(ResourceLocation soundName, SoundCategory soundCategory,
                                             Vector3i effectPosition, byte[] remainingData) {
        // Removed around 1.19.2ish
        super(Server.NAMED_SOUND_EFFECT);
        this.soundName = soundName;
        this.soundCategory = soundCategory;
        this.effectPosition = effectPosition;
        this.remainingData = remainingData;
    }

    public void read() {
        // CO: Heavily based on PacketEvents builtin WrapperPlayServerSoundEffect
        this.soundName = this.readIdentifier();
        this.soundCategory = SoundCategory.fromId(readVarInt());
        effectPosition = new Vector3i(readInt(), readInt(), readInt());
        this.remainingData = this.readRemainingBytes();
    }

    public void write() {
        this.writeIdentifier(this.soundName);
        this.writeVarInt(soundCategory.ordinal());
        writeInt(effectPosition.x);
        writeInt(effectPosition.y);
        writeInt(effectPosition.z);
        this.writeBytes(this.remainingData);
    }

    public Vector3i getEffectPosition() {
        return effectPosition;
    }

    public void setEffectPosition(Vector3i effectPosition) {
        this.effectPosition = effectPosition;
    }
}
