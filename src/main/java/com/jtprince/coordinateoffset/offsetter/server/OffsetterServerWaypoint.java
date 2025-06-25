package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.world.waypoint.*;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWaypoint;
import com.jtprince.coordinateoffset.CoordinateOffset;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;

public class OffsetterServerWaypoint extends PacketOffsetter<WrapperPlayServerWaypoint> {
    public OffsetterServerWaypoint() {
        super(WrapperPlayServerWaypoint.class, PacketType.Play.Server.WAYPOINT);
    }

    @Override
    public void offset(WrapperPlayServerWaypoint packet, Offset offset, User user) {
        TrackedWaypoint waypoint = packet.getWaypoint();

        WaypointInfo oldInfo = waypoint.getInfo();
        WaypointInfo newInfo;
        if (oldInfo instanceof Vec3iWaypointInfo info) {
            newInfo = new Vec3iWaypointInfo(apply(info.getPosition(), offset));
        } else if (oldInfo instanceof ChunkWaypointInfo info) {
            newInfo = new ChunkWaypointInfo(applyChunkX(info.getChunkX(), offset), applyChunkZ(info.getChunkZ(), offset));
        } else if (oldInfo instanceof AzimuthWaypointInfo || oldInfo instanceof EmptyWaypointInfo) {
            // No offset needed for these types
            return;
        } else {
            CoordinateOffset.getInstance().getLogger().warning("Unknown waypoint type: " + oldInfo.getClass().getName());
            return;
        }

        packet.setWaypoint(new TrackedWaypoint(waypoint.getIdentifier(), waypoint.getIcon(), newInfo));
    }
}
