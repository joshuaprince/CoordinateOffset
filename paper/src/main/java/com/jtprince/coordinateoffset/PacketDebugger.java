package com.jtprince.coordinateoffset;

import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.User;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * In debug mode, the plugin can log the types of every packet sent and received to get more details about what led
 * up to any given error.
 */
public class PacketDebugger {
    private final CoordinateOffset coPlugin;

    public PacketDebugger(CoordinateOffset coPlugin) {
        this.coPlugin = coPlugin;
    }

    private static class LoggedPacket {
        private final PacketTypeCommon type;
        private int consecutiveCount = 1;
        private LoggedPacket(PacketTypeCommon type) {
            this.type = type;
        }
        @Override
        public String toString() {
            char directionIndicator = '?';
            switch (type.getSide()) {
                case SERVER -> directionIndicator = '>';
                case CLIENT -> directionIndicator = '<';
            }
            return directionIndicator + type.getName() + '(' + consecutiveCount + ')';
        }
    }
    private final Map<User, Deque<LoggedPacket>> history = new HashMap<>();

    public void logPacket(User user, PacketTypeCommon type) {
        Deque<LoggedPacket> deque = history.computeIfAbsent(user, (u) -> new ArrayDeque<>(getDebugHistorySize()));

        // If this is the same packet type as the last one, just increment the counter instead of logging a new packet.
        LoggedPacket last = deque.peekLast();
        if (last != null && last.type == type) {
            last.consecutiveCount++;
            return;
        }

        // Prevent the deque from getting too big.
        if (deque.size() >= getDebugHistorySize()) {
            deque.removeFirst();
        }

        deque.addLast(new LoggedPacket(type));
    }

    public String getHistory(User user) {
        Deque<LoggedPacket> deque = history.get(user);
        if (deque == null) return "[]";
        return deque.toString();
    }

    public void forget(User user) {
        history.remove(user);
    }

    private int getDebugHistorySize() {
        return coPlugin.getConfig().getInt("debug.packetHistorySize", 8);
    }
}
