package com.jtprince.coordinateoffset.offsetter.plugin;

import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPluginMessage;
import com.jtprince.coordinateoffset.CoordinateOffsetCore;
import com.jtprince.coordinateoffset.FixedOffset;
import org.jspecify.annotations.NullMarked;

import java.nio.charset.StandardCharsets;
import java.util.Set;

@NullMarked
public class PluginOffsetterWorldEditCUI implements OffsetterPluginMessage.PluginOffsetter {
    private static final Set<String> CHANNELS = Set.of("worldedit:cui");
    @Override
    public Set<String> getHandledChannels() {
        return CHANNELS;
    }

    @Override
    public OffsetterPluginMessage.PluginOffsetter.Server getServerOffsetter() {
        return new Server();
    }
    public static class Server extends OffsetterPluginMessage.PluginOffsetter.Server {
        @Override
        public void offset(WrapperPlayServerPluginMessage packet, FixedOffset offset, User user) {
            // Decode with ISO-8859-1 to avoid issues with non-ASCII characters and passthrough them when re-encoding
            String data = new String(packet.getData(), StandardCharsets.ISO_8859_1);
            String[] segments = data.split("\\|");

            if (CoordinateOffsetCore.get().isDebugEnabled()) {
                CoordinateOffsetCore.get().getLogger().info("Outgoing WorldEditCUI plugin message: " + data);
            }

            /*
             * Formats are defined by WorldEdit. The first substring defines which format to use. All formats are here:
             * WorldEdit (see subclasses, getTypeId): https://github.com/EngineHub/WorldEdit/blob/master/worldedit-core/src/main/java/com/sk89q/worldedit/internal/cui/CUIEvent.java
             * WorldEditCUI client code: https://github.com/EngineHub/WorldEditCUI/blob/master/worldeditcui-fabric/src/main/java/org/enginehub/worldeditcui/event/CUIEventType.java
             */
            switch (segments[0]) {
                case "cyl" -> {
                    // SelectionCylinderEvent/CYLINDER: Set cylindrical region, format "cyl|x|y|z|xradius|zradius"
                    int x = Integer.parseInt(segments[1]);
                    int z = Integer.parseInt(segments[3]);
                    segments[1] = String.valueOf(x - offset.x());
                    segments[3] = String.valueOf(z - offset.z());
                }
                case "e" -> {
                    // SelectionEllipsoidPointEvent/ELLIPSOID: Set ellipsoid region, format "e|0|x|y|z" for center point
                    // and "e|1|xradius|yradius|zradius" for extents (extents should NOT be offsetted)
                    if (!segments[1].equals("0")) return;

                    int x = Integer.parseInt(segments[2]);
                    int z = Integer.parseInt(segments[4]);
                    segments[2] = String.valueOf(x - offset.x());
                    segments[4] = String.valueOf(z - offset.z());
                }
                case "mm" -> {
                    // SelectionMinMaxEvent/MINMAX: Set cylindrical or polygon region Y coordinates, format "mm|ymin|ymax"
                    // (no X or Z coordinates to offset)
                    return;
                }
                case "p" -> {
                    // SelectionPointEvent/POINT: Set cuboid point, format "p|index|x|y|z|area"
                    int x = Integer.parseInt(segments[2]);
                    int z = Integer.parseInt(segments[4]);
                    segments[2] = String.valueOf(x - offset.x());
                    segments[4] = String.valueOf(z - offset.z());
                }
                case "p2" -> {
                    // SelectionPoint2DEvent/POINT2D: Set polygon point, format "p2|index|x|z|area"
                    int x = Integer.parseInt(segments[2]);
                    int z = Integer.parseInt(segments[3]);
                    segments[2] = String.valueOf(x - offset.x());
                    segments[3] = String.valueOf(z - offset.z());
                }
                case "poly" -> {
                    // SelectionPolygonEvent/POLYGON: Set list of vertex connection indices; not raw coordinates
                    return;
                }
                case "s" -> {
                    // SelectionShapeEvent/SELECTION: Set selection shape, e.g. "s|cuboid"
                    return;
                }

                case "col", "grid", "u" -> {
                    // These cases are defined in WorldEditCUI as "COLOUR", "GRID", and "UPDATE" respectively.
                    // They don't appear to be used by WorldEdit. In WECUI, they don't appear to contain coordinates.
                    // Assume they don't need offsetting and return to avoid the default warning.
                    return;
                }

                default -> {
                    String safeData = new String(packet.getData(), StandardCharsets.US_ASCII);
                    CoordinateOffsetCore.get().getLogger().warning("Failed to offset an outgoing WorldEditCUI plugin message: " + safeData);
                    return;
                }
            }

            packet.setData(String.join("|", segments).getBytes(StandardCharsets.ISO_8859_1));
        }
    }
}
