package com.jtprince.coordinateoffset.offsetter.plugin;

import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPluginMessage;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPluginMessage;
import com.jtprince.coordinateoffset.CoordinateOffsetCore;
import com.jtprince.coordinateoffset.FixedOffset;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@NullMarked
public class PluginOffsetterDistantHorizons implements OffsetterPluginMessage.PluginOffsetter {
    private static final String CHANNEL = "distant_horizons:message";
    private static final Set<String> CHANNELS = Set.of(CHANNEL);

    /* DHS protocol version is defined in DHS plugin PluginMessageHandler class  */
    private static final short SUPPORTED_PROTOCOL_VERSION_MIN = 11;
    private static final short SUPPORTED_PROTOCOL_VERSION_MAX = 13;

    /* IDs below are defined in DHS plugin PluginMessageHandler class */
    private static final int DH_MSG_ID_CONFIG = 3;
    private static final int DH_MSG_ID_EXCEPTION_MSG = 5;
    private static final int DH_MSG_ID_FULL_DATA_SOURCE_REQUEST = 6;
    private static final int DH_MSG_ID_FULL_DATA_SOURCE_RESPONSE = 7;
    private static final int DH_MSG_ID_FULL_DATA_PARTIAL_UPDATE = 8;
    private static final int DH_MSG_ID_FULL_DATA_CHUNK = 9;

    /* Exception message IDs. Defined in DHS plugin ExceptionMessage class */
    private static final int DH_MSG_EXCEPTION_SECTION_REQUIRES_SPLITTING = 3;

    @Override
    public Set<String> getHandledChannels() {
        return CHANNELS;
    }

    @Override
    public OffsetterPluginMessage.PluginOffsetter.Server getServerOffsetter() {
        return new Server();
    }
    public class Server extends OffsetterPluginMessage.PluginOffsetter.Server {
        @Override
        public void offset(WrapperPlayServerPluginMessage packet, FixedOffset offset, User user) {
            ByteBuf data = Unpooled.wrappedBuffer(packet.getData());

            short protocolVersion = data.readShort();
            short messageTypeId = data.readShort();

            if (protocolVersion < SUPPORTED_PROTOCOL_VERSION_MIN || protocolVersion > SUPPORTED_PROTOCOL_VERSION_MAX) {
                if (offset.isZero()) return;
                PlayerWarningCache warningCache = playerWarningCache.computeIfAbsent(user.getUUID(), k -> new PlayerWarningCache());
                if (!warningCache.hasWarnedProtocolVersion) {
                    warningCache.hasWarnedProtocolVersion = true;
                    CoordinateOffsetCore.get().getLogger().warning("A version of the Distant Horizons Support " +
                        "plugin (DHSupport) is installed which is not compatible with this version of " +
                        "CoordinateOffset. Player " + user.getName() + " may experience issues receiving LODs from " +
                        "the server while they have an offset applied. " +
                        (protocolVersion < SUPPORTED_PROTOCOL_VERSION_MIN ?
                            "Update DHSupport to resolve this issue." :
                            "Please update CoordinateOffset, or inform the CoordinateOffset developer of this issue " +
                                "if you are already on the latest version.") +
                        " (DHS protocol version = " + protocolVersion + ", CoordinateOffset supported versions = "
                        + SUPPORTED_PROTOCOL_VERSION_MIN + "-" + SUPPORTED_PROTOCOL_VERSION_MAX + ")");
                }
            }

            if (messageTypeId == DH_MSG_ID_CONFIG) {
                boolean distantGenerationEnabled = data.readBoolean();
                int renderDistance = data.readInt();

                int borderCenterX, borderCenterZ, borderRadius;
                if (CoordinateOffsetCore.get().getConfig().getObfuscateWorldBorder()) {
                    // Hide borders from packets - client may request out of bounds LODs and get errors
                    data.setInt(data.readerIndex(), 0);
                    borderCenterX = data.readInt();

                    data.setInt(data.readerIndex(), 0);
                    borderCenterZ = data.readInt();

                    data.setInt(data.readerIndex(), 30_000_000);
                    borderRadius = data.readInt();
                } else {
                    // Offset borders appropriately
                    borderCenterX = data.getInt(data.readerIndex());
                    data.setInt(data.readerIndex(), borderCenterX - offset.x());
                    borderCenterX = data.readInt();

                    borderCenterZ = data.getInt(data.readerIndex());
                    data.setInt(data.readerIndex(), borderCenterZ - offset.z());
                    borderCenterZ = data.readInt();

                    borderRadius = data.readInt();
                }

                if (CoordinateOffsetCore.get().isDebugEnabled()) {
                    CoordinateOffsetCore.get().getLogger().info(String.format("Outgoing Distant Horizons config message: " +
                            "distantGenerationEnabled=%b, renderDistance=%d, borderCenterX=%d, borderCenterZ=%d, borderRadius=%d",
                        distantGenerationEnabled, renderDistance, borderCenterX, borderCenterZ, borderRadius));
                }
            }

            if (messageTypeId == DH_MSG_ID_EXCEPTION_MSG) {
                int tracker = data.readInt();
                int typeId = data.readInt();
                short messageLen = data.readShort();
                String exceptionMessage = data.readCharSequence(messageLen, StandardCharsets.UTF_8).toString();
                exceptionMessage.getBytes();
            }

            if (messageTypeId == DH_MSG_ID_FULL_DATA_SOURCE_RESPONSE) {
                int tracker = data.readInt();
                boolean hasBufferId = data.readBoolean();
                if (hasBufferId) {
                    int bufferId = data.readInt();
                    int numBeacons = data.readInt();
                    for (int i = 0; i < numBeacons; i++) {
                        int x = data.readInt();
                        data.setInt(data.readerIndex() - 4, x - offset.x());
                        int y = data.readInt();
                        int z = data.readInt();
                        data.setInt(data.readerIndex() - 4, z - offset.z());
                        int color = data.readInt();
                    }
                }
            }

            if (messageTypeId == DH_MSG_ID_FULL_DATA_PARTIAL_UPDATE) {
                short worldNameLen = data.readShort();
                data.skipBytes(worldNameLen);

                int bufferId = data.readInt();
                int numBeacons = data.readInt();
                for (int i = 0; i < numBeacons; i++) {
                    int x = data.readInt();
                    data.setInt(data.readerIndex() - 4, x - offset.x());
                    int y = data.readInt();
                    int z = data.readInt();
                    data.setInt(data.readerIndex() - 4, z - offset.z());
                    int color = data.readInt();
                }
            }

            if (messageTypeId == DH_MSG_ID_FULL_DATA_CHUNK) {
                int bufferId = data.readInt();
                int dataLength = data.readInt();

                /*
                 * DH data chunk messages may be split into multiple packets.
                 * Only the first packet contains a section position.
                 * The last byte of the data indicates if this is the first packet.
                 */
                boolean isFirst = data.getBoolean(data.capacity() - 1);
                if (isFirst) {
                    long sectionPosition = data.getLong(data.readerIndex());
                    DhSectionPosition sectionPositionObj = DhSectionPosition.fromLong(sectionPosition);
                    DhSectionPosition offsetted = sectionPositionObj.offset(offset);
                    long offsettedLong = offsetted.toLong(); // TODO null check
                    data.setLong(data.readerIndex(), offsettedLong);
                }
            }

            data.release();
        }
    }

    @Override
    public OffsetterPluginMessage.PluginOffsetter.Client getClientOffsetter() {
        return new Client();
    }
    public class Client extends OffsetterPluginMessage.PluginOffsetter.Client {
        @Override
        public void offset(WrapperPlayClientPluginMessage packet, FixedOffset offset, User user) {
            ByteBuf data = Unpooled.wrappedBuffer(packet.getData());

            short protocolVersion = data.readShort();
            short messageTypeId = data.readShort();

            if (messageTypeId == DH_MSG_ID_CONFIG && CoordinateOffsetCore.get().isDebugEnabled()) {
                printConfig(data, true);
            }

            if (messageTypeId == DH_MSG_ID_FULL_DATA_SOURCE_REQUEST) {
                int tracker = data.readInt();
                short worldNameLen = data.readShort();
                data.skipBytes(worldNameLen);

                long sectionPosition = data.getLong(data.readerIndex());
                DhSectionPosition sectionPositionObj = DhSectionPosition.fromLong(sectionPosition);
                DhSectionPosition unOffsetted = sectionPositionObj.offset(offset.negate());

                if (unOffsetted == null) {
                    /*
                     * The client may request a detail level that is not 6.
                     * This poses a problem with offset alignment; to accurately apply or unapply an offset, the offset
                     * must be exactly divisible by (2^(detail level)).
                     * CoordinateOffset tries its best to ensure offsets are divisible by 64 (2^6) when DHS is
                     * installed, but if the client requests detail level 9, offsets that aren't also divisible by 512
                     * cannot be applied to those requests.
                     * For now, DHS itself supports detail level 6 ONLY. We can avoid trying to offset any request that
                     * DHS would reject anyway by dropping the request and sending the "exception" response ourselves.
                     */
                    packet.setChannelName(CHANNEL + "_cancelled_by_coordinateoffset"); // This "cancels" the packet

                    ByteBuf responseData = Unpooled.buffer();
                    responseData.writeShort(protocolVersion);
                    responseData.writeShort(DH_MSG_ID_EXCEPTION_MSG);
                    responseData.writeInt(tracker);
                    String exceptionMessage = "Only detail level 6 is supported"; // Match DHS LodHandler.java message
                    responseData.writeShort((short) exceptionMessage.length());
                    responseData.writeBytes(exceptionMessage.getBytes());

                    WrapperPlayServerPluginMessage responseMsg = new WrapperPlayServerPluginMessage(CHANNEL, responseData.array());
                    user.sendPacket(responseMsg);
                    responseData.release();
                } else {
                    long unOffsettedLong = unOffsetted.toLong();
                    data.setLong(data.readerIndex(), unOffsettedLong);
                }
            }

            data.release();
        }
    }

    /**
     * Distant Horizons splits the world into quadtree "sections" of varying detail. Each position is packed into a
     * long, with bits making up the components listed in this record.
     *
     * @param detailLevel Scale factor for x and z. Coordinate components are multiplied by (2^(detail level)).
     *                    As of DHSupport protocol 13, the server plugin always sends detail level 6 for a 64x64-block
     *                    section.
     * @param x X-coordinate of the section. Multiply by (2^(detail level)) to get the actual x-coordinate.
     * @param z Z-coordinate of the section. Multiply by (2^(detail level)) to get the actual z-coordinate.
     */
    record DhSectionPosition(int detailLevel, int x, int z) {
        static DhSectionPosition fromLong(long sectionPosition) {
            // lowest 8 bits - detail level
            int detailLevel = (int) (sectionPosition & 0xFF);
            // middle 28 bits - x
            int x = (int) ((sectionPosition >> 8) & 0x0FFFFFFF);
            if ((x & (1 << 27)) != 0) {
                x |= ~0x0FFFFFFF;
            }
            // upper 28 bits - z
            int z = (int) ((sectionPosition >> 36) & 0x0FFFFFFF);
            if ((z & (1 << 27)) != 0) {
                z |= ~0x0FFFFFFF;
            }
            return new DhSectionPosition(detailLevel, x, z);
        }

        long toLong() {
            long data = 0;
            data |= (detailLevel & 0xFFL);
            data |= (x & 0x0FFFFFFFL) << 8;
            data |= (z & 0x0FFFFFFFL) << 36;
            return data;
        }

        /**
         * Apply an offset to this section position.
         * @param offset The offset to apply (subtract).
         * @return A new section position, or null if either component cannot be offset because the component is not
         *         a multiple of (2^(detail level)).
         */
        @Nullable DhSectionPosition offset(FixedOffset offset) {
            if (offset.x() % (1 << detailLevel) != 0) {
                return null;
            }
            if (offset.z() % (1 << detailLevel) != 0) {
                return null;
            }

            return new DhSectionPosition(detailLevel, x - (offset.x() >> detailLevel), z - (offset.z() >> detailLevel));
        }
    }

    /**
     * Cache container for deduplicating warning messages per player.
     */
    private static class PlayerWarningCache {
        boolean hasWarnedProtocolVersion = false;
        boolean hasWarnedOffsetMultiple = false;
    }
    private final ConcurrentHashMap<UUID, PlayerWarningCache> playerWarningCache = new ConcurrentHashMap<>();

    @Override
    public void onUserDisconnect(User user) {
        playerWarningCache.remove(user.getUUID());
    }

    static void printConfig(ByteBuf data, boolean isIncoming) {
        boolean distantGenerationEnabled = data.readBoolean();
        int renderDistance = data.readInt();
        int borderCenterX = data.readInt();
        int borderCenterZ = data.readInt();
        int borderRadius = data.readInt();
        int fullDataRequestConcurrencyLimit = data.readInt();
        boolean realTimeUpdatesEnabled = data.readBoolean();
        int realTimeUpdateRadius = data.readInt();
        boolean loginDataSyncEnabled = data.readBoolean();
        int loginDataSyncRadius = data.readInt();
        int loginDataSyncRcLimit = data.readInt();
        int maxDataTransferSpeed = data.readInt();

        CoordinateOffsetCore.get().getLogger().info(String.format((isIncoming ? "Incoming" : "Outgoing") + " config message from Distant Horizons: " +
                "distantGenerationEnabled=%b, renderDistance=%d, borderCenterX=%d, borderCenterZ=%d, borderRadius=%d, fullDataRequestConcurrencyLimit=%d, realTimeUpdatesEnabled=%b, realTimeUpdateRadius=%d, loginDataSyncEnabled=%b, loginDataSyncRadius=%d, loginDataSyncRcLimit=%d, maxDataTransferSpeed=%d",
            distantGenerationEnabled, renderDistance, borderCenterX, borderCenterZ, borderRadius, fullDataRequestConcurrencyLimit, realTimeUpdatesEnabled, realTimeUpdateRadius, loginDataSyncEnabled, loginDataSyncRadius, loginDataSyncRcLimit, maxDataTransferSpeed));
    }
}
