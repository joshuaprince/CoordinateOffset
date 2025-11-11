package com.jtprince.coordinateoffset.offsetter.server;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUnloadChunk;
import com.jtprince.coordinateoffset.FixedOffset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class OffsetterServerUnloadChunk extends PacketOffsetter<WrapperPlayServerUnloadChunk> {
    public OffsetterServerUnloadChunk() {
        super(WrapperPlayServerUnloadChunk.class, PacketType.Play.Server.UNLOAD_CHUNK);
    }

    @Override
    public void offset(WrapperPlayServerUnloadChunk packet, FixedOffset offset, User user) {
        packet.setChunkX(applyChunkX(packet.getChunkX(), offset));
        packet.setChunkZ(applyChunkZ(packet.getChunkZ(), offset));
    }
}
