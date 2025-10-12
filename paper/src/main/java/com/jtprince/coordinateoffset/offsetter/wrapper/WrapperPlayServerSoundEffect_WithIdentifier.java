/*
 * This file is part of packetevents - https://github.com/retrooper/packetevents
 * Copyright (C) 2022 retrooper and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jtprince.coordinateoffset.offsetter.wrapper;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.sound.SoundCategory;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

import javax.annotation.Nullable;

/*
 * In 1.19.3, the NAMED_SOUND_EFFECT packet was merged with the standard SOUND_EFFECT packet and 3 optional fields added:
 *  - Sound Name (Optional Identifier, only present if Sound ID is 0)
 *  - Has Fixed Range (Optional Boolean, only present if Sound ID is 0)
 *  - Range (Optional Float, only present if Sound ID is 0 AND Has Fixed Range is true)
 * source: https://prismarinejs.github.io/minecraft-data/protocol/pc/1.19.3/#play.toClient.types.packet_sound_effect
 *
 * These fields are not currently included in the PacketEvents-provided wrapper. I copied that wrapper here and added
 * the code marked with "CO" so that these packets can be properly parsed and offsetted.
 *
 * This only became necessary when I started testing Model Engine, which uses custom named sound effects. It may also
 * be necessary for /playsound commands.
 */
@SuppressWarnings("unused") // Constructors are called reflectively
public class WrapperPlayServerSoundEffect_WithIdentifier extends PacketWrapper<WrapperPlayServerSoundEffect_WithIdentifier> {
    private int soundID;
    // CO Start
    private @Nullable ResourceLocation soundName;
    private @Nullable Boolean hasFixedRange;
    private @Nullable Float range;
    // CO End
    private SoundCategory soundCategory;
    private Vector3i effectPosition;
    private float volume;
    private float pitch;
    private long seed;

    public WrapperPlayServerSoundEffect_WithIdentifier(PacketSendEvent event) {
        super(event);
    }

    public WrapperPlayServerSoundEffect_WithIdentifier(int soundID, SoundCategory soundCategory,
                                                       Vector3i effectPosition, float volume, float pitch) {
        this(soundID, soundCategory, effectPosition, volume, pitch, -1);
    }

    public WrapperPlayServerSoundEffect_WithIdentifier(int soundID, SoundCategory soundCategory,
                                                       Vector3i effectPosition, float volume, float pitch, long seed) {
        super(PacketType.Play.Server.SOUND_EFFECT);
        this.soundID = soundID;
        this.soundCategory = soundCategory;
        this.effectPosition = effectPosition;
        this.volume = volume;
        this.pitch = pitch;
        this.seed = seed;
    }

    // CO TBD: Add constructors that include these added fields.

    @Override
    public void read() {
        soundID = readVarInt();
        // CO Start
        if (soundID == 0) {
            soundName = readIdentifier();
            hasFixedRange = readBoolean();
            if (hasFixedRange) {
                range = readFloat();
            }
        }
        // CO End
        soundCategory = SoundCategory.fromId(readVarInt());
        effectPosition = new Vector3i(readInt(), readInt(), readInt());
        volume = readFloat();
        pitch = readFloat();
        if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_19)) {
            this.seed = readLong();
        }
    }

    @Override
    public void write() {
        writeVarInt(soundID);
        // CO Start
        if (soundName != null) {
            writeIdentifier(soundName);
        }
        if (hasFixedRange != null) {
            writeBoolean(hasFixedRange);
            if (range != null) {
                writeFloat(range);
            }
        }
        // CO End
        writeVarInt(soundCategory.ordinal());
        writeInt(effectPosition.x);
        writeInt(effectPosition.y);
        writeInt(effectPosition.z);
        writeFloat(volume);
        writeFloat(pitch);
        if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_19)) {
            writeLong(seed);
        }
    }

    @Override
    public void copy(WrapperPlayServerSoundEffect_WithIdentifier wrapper) {
        soundID = wrapper.soundID;
        soundCategory = wrapper.soundCategory;
        effectPosition = wrapper.effectPosition;
        volume = wrapper.volume;
        pitch = wrapper.pitch;
        seed = wrapper.seed;
    }

    public int getSoundId() {
        return soundID;
    }

    public void setSoundId(int soundID) {
        this.soundID = soundID;
    }

    // CO TBD: Add getters and setters for the added fields.

    public SoundCategory getSoundCategory() {
        return soundCategory;
    }

    public void setSoundCategory(SoundCategory soundCategory) {
        this.soundCategory = soundCategory;
    }

    public Vector3i getEffectPosition() {
        return effectPosition;
    }

    public void setEffectPosition(Vector3i effectPosition) {
        this.effectPosition = effectPosition;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    /**
     * Seeds only exist in server version 1.19+
     *
     * @return the seed
     */
    public long getSeed() {
        return seed;
    }

    /**
     * Seeds only exist in server version 1.19+
     *
     * @param seed the seed to set
     */
    public void setSeed(long seed) {
        this.seed = seed;
    }
}
