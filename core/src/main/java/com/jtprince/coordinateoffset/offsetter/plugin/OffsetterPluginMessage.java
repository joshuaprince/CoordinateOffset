package com.jtprince.coordinateoffset.offsetter.plugin;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPluginMessage;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPluginMessage;
import com.jtprince.coordinateoffset.FixedOffset;
import com.jtprince.coordinateoffset.offsetter.PacketOffsetter;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Container for offsetters of <code>minecraft:plugin_message</code> packets in both server and client directions.
 *
 * <p>Plugin messages have varying channel names and payload formats, requiring specialized offsetters for each.
 * This class provides an abstract outline for defining new "Plugin Offsetters", keyed on the channel name used by
 * any given plugin message.</p>
 *
 * <p>To define a new offsetter for a plugin message payload, see the other {@link PluginOffsetter} implementations
 * in this module and add any new ones to {@link #PLUGIN_OFFSETTER_OFFSETTERS}.</p>
 */
@NullMarked
public final class OffsetterPluginMessage {
    /** Registry of all {@link PluginOffsetter} implementations. Add new ones here. */
    private static final List<PluginOffsetter> PLUGIN_OFFSETTER_OFFSETTERS = List.of(
        new PluginOffsetterWorldEditCUI()
    );

    private static final Map<String, PluginOffsetter.Client> byChannelNameClient;
    private static final Map<String, PluginOffsetter.Server> byChannelNameServer;
    static  {
        try {
            byChannelNameClient = new HashMap<>();
            byChannelNameServer = new HashMap<>();
            for (PluginOffsetter offsetter : PLUGIN_OFFSETTER_OFFSETTERS) {
                PluginOffsetter.Client client = offsetter.getClientOffsetter();
                PluginOffsetter.Server server = offsetter.getServerOffsetter();
                for (String channel : offsetter.getHandledChannels()) {
                    if (client != null) {
                        byChannelNameClient.put(channel, client);
                    }
                    if (server != null) {
                        byChannelNameServer.put(channel, server);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Stacktraces thrown in static blocks are not logged
            throw new RuntimeException(e);
        }
    }

    private OffsetterPluginMessage() {} // Static-only outer class

    public static class Client extends PacketOffsetter<WrapperPlayClientPluginMessage> {
        public Client() {
            super(WrapperPlayClientPluginMessage.class, PacketType.Play.Client.PLUGIN_MESSAGE);
        }

        @Override
        public void offset(WrapperPlayClientPluginMessage packet, FixedOffset offset, User user) {
            PluginOffsetter.Client offsetter = byChannelNameClient.get(packet.getChannelName());
            if (offsetter != null) {
                offsetter.offset(packet, offset, user);
            }
        }
    }

    public static class Server extends PacketOffsetter<WrapperPlayServerPluginMessage> {
        public Server() {
            super(WrapperPlayServerPluginMessage.class, PacketType.Play.Server.PLUGIN_MESSAGE);
        }

        @Override
        public void offset(WrapperPlayServerPluginMessage packet, FixedOffset offset, User user) {
            PluginOffsetter.Server offsetter = byChannelNameServer.get(packet.getChannelName());
            if (offsetter != null) {
                offsetter.offset(packet, offset, user);
            }
        }
    }

    /**
     * Interface for plugin message offsetters. Define one per plugin/mod that uses one or more plugin message channels.
     */
    public interface PluginOffsetter {
        Set<String> getHandledChannels();
        default @Nullable Client getClientOffsetter() { return null; }
        default @Nullable Server getServerOffsetter() { return null; }

        abstract class Client extends PacketOffsetter<WrapperPlayClientPluginMessage> {
            public Client() {
                super(WrapperPlayClientPluginMessage.class, PacketType.Play.Client.PLUGIN_MESSAGE);
            }
        }

        abstract class Server extends PacketOffsetter<WrapperPlayServerPluginMessage> {
            public Server() {
                super(WrapperPlayServerPluginMessage.class, PacketType.Play.Server.PLUGIN_MESSAGE);
            }
        }
    }
}
