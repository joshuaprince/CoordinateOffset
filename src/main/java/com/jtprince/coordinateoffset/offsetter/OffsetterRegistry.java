package com.jtprince.coordinateoffset.offsetter;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.client.*;
import com.jtprince.coordinateoffset.offsetter.server.*;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"rawtypes", "unchecked"})
public class OffsetterRegistry {
    private static final Map<PacketTypeCommon, PacketOffsetter> byPacketType;

    private static final List<PacketOffsetter> offsetters = List.of(
            new OffsetterClientGenerateStructure(),
            new OffsetterClientPlayerBlockPlacement(),
            new OffsetterClientPlayerDigging(),
            new OffsetterClientPlayerPosition(),
            new OffsetterClientUpdateCommandBlock(),
            new OffsetterClientUpdateJigsawBlock(),
            new OffsetterClientUpdateSign(),
            new OffsetterClientVehicleMove(),

            new OffsetterServerAcknowledgePlayerDigging(),
            new OffsetterServerBlockAction(),
            new OffsetterServerBlockBreakAnimation(),
            new OffsetterServerBlockChange(),
            new OffsetterServerBlockEntityData(),
            new OffsetterServerChunkData(),
            new OffsetterServerEffect(),
            new OffsetterServerEntityMetadata(),
            new OffsetterServerEntityTeleport(),
            new OffsetterServerExplosion(),
            new OffsetterServerFacePlayer(),
            new OffsetterServerJoinGame(),
            new OffsetterServerUpdateLight(),
            new OffsetterServerMultiBlockChange(),
            new OffsetterServerNamedSoundEffect(),
            new OffsetterServerOpenSignEditor(),
            new OffsetterServerParticle(),
            new OffsetterServerPlayerPositionAndLook(),
            new OffsetterServerRespawn(),
            new OffsetterServerSculkVibrationSignal(),
            new OffsetterServerSetSlot(),
            new OffsetterServerSoundEffect(),
            new OffsetterServerSpawnEntity(),
            new OffsetterServerSpawnExperienceOrb(),
            new OffsetterServerSpawnLivingEntity(),
            new OffsetterServerSpawnPainting(),
            new OffsetterServerSpawnPlayer(),
            new OffsetterServerSpawnPosition(),
            new OffsetterServerUnloadChunk(),
            new OffsetterServerUpdateViewPosition(),
            new OffsetterServerVehicleMove(),
            new OffsetterServerWindowItems()
    );

    static  {
        byPacketType = new HashMap<>();
        for (PacketOffsetter offsetter : offsetters) {
            for (PacketTypeCommon type : offsetter.packetTypes) {
                byPacketType.put(type, offsetter);
            }
        }
    }

    public static void attemptToOffset(PacketSendEvent event, Offset offset) {
        PacketOffsetter associatedOffsetter = byPacketType.get(event.getPacketType());
        if (associatedOffsetter == null) return;

        try {
            PacketWrapper wrapper = (PacketWrapper) associatedOffsetter.wrapperClass.getConstructor(PacketSendEvent.class).newInstance(event);
            associatedOffsetter.offset(wrapper, offset, event.getUser());
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void attemptToUnOffset(PacketReceiveEvent event, Offset offset) {
        PacketOffsetter associatedOffsetter = byPacketType.get(event.getPacketType());
        if (associatedOffsetter == null) return;

        try {
            PacketWrapper wrapper = (PacketWrapper) associatedOffsetter.wrapperClass.getConstructor(PacketReceiveEvent.class).newInstance(event);
            associatedOffsetter.offset(wrapper, offset, event.getUser());
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
