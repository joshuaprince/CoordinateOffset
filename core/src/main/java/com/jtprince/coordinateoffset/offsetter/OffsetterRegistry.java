package com.jtprince.coordinateoffset.offsetter;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.jtprince.coordinateoffset.FixedOffset;
import com.jtprince.coordinateoffset.offsetter.client.*;
import com.jtprince.coordinateoffset.offsetter.server.*;
import org.jspecify.annotations.NullMarked;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"rawtypes", "unchecked"})
@NullMarked
public class OffsetterRegistry {
    private static final Map<PacketTypeCommon, PacketOffsetter> byPacketType;

    private static final List<PacketOffsetter> offsetters = List.of(
            /*
             * NOTE: Only PLAY packets are supported. The server can send LOGIN and CONFIGURATION packets before an
             * offset is applied. If other packet type offsets are needed, be sure to update PacketOffsetAdapter to
             * handle those packet types.
             */
            new OffsetterClientClickWindow(),
            new OffsetterClientCreativeInventoryAction(),
            new OffsetterClientGenerateStructure(),
            new OffsetterClientPickItemFromBlock(),
            new OffsetterClientPlayerBlockPlacement(),
            new OffsetterClientPlayerDigging(),
            new OffsetterClientPlayerPosition(),
            new OffsetterClientSetStructureBlock(),
            new OffsetterClientSetTestBlock(),
            new OffsetterClientTestInstanceBlockAction(),
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
            // Start debug packets - completely disabled by default
            new OffsetterServerDebugBlockValue(),
            new OffsetterServerDebugChunkValue(),
            // new OffsetterServerDebugEntityValue(), // TODO: see class file
            // new OffsetterServerDebugEvent(),       // TODO: see class file
            // new OffsetterServerDebugSample(),      // TODO: see class file
            // End debug packets
            new OffsetterServerEffect(),
            new OffsetterServerEntityEquipment(),
            new OffsetterServerEntityMetadata(),
            new OffsetterServerEntityPositionSync(),
            new OffsetterServerEntityTeleport(),
            new OffsetterServerExplosion(),
            new OffsetterServerFacePlayer(),
            new OffsetterServerGameTestHighlightPos(),
            new OffsetterServerJoinGame(),
            new OffsetterServerUpdateLight(),
            new OffsetterServerMoveMinecart(),
            new OffsetterServerMultiBlockChange(),
            new OffsetterServerNamedSoundEffect(),
            new OffsetterServerOpenSignEditor(),
            new OffsetterServerParticle(),
            new OffsetterServerPlayerPositionAndLook(),
            new OffsetterServerRespawn(),
            new OffsetterServerSetCursorItem(),
            new OffsetterServerSetPlayerInventory(),
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
            new OffsetterServerWaypoint(),
            new OffsetterServerWindowItems()
    );

    static  {
        try {
            byPacketType = new HashMap<>();
            for (PacketOffsetter offsetter : offsetters) {
                for (PacketTypeCommon type : offsetter.packetTypes) {
                    byPacketType.put(type, offsetter);
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Stacktraces thrown in static blocks are not logged
            throw new RuntimeException(e);
        }
    }

    public static void attemptToOffset(PacketSendEvent event, FixedOffset offset) {
        PacketOffsetter associatedOffsetter = byPacketType.get(event.getPacketType());
        if (associatedOffsetter == null) return;

        try {
            PacketWrapper wrapper = (PacketWrapper) associatedOffsetter.wrapperClass.getConstructor(PacketSendEvent.class).newInstance(event);
            associatedOffsetter.offset(wrapper, offset, event.getUser());
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void attemptToUnOffset(PacketReceiveEvent event, FixedOffset offset) {
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
