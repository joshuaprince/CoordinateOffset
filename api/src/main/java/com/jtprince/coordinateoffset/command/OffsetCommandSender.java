package com.jtprince.coordinateoffset.command;

import com.jtprince.coordinateoffset.adapter.OffsetPlayer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collections;

/**
 * Adapter interface representing a command sender in a Minecraft server.
 *
 * @param audience An audience that can be used to respond to this sender.
 * @param name String to represent the sender in various verbose messages.
 * @param player The player who sent this command, or <code>null</code> if the sender is not a player (console, command
 *               block, etc.)
 */
@NullMarked
public record OffsetCommandSender(Audience audience, String name, @Nullable OffsetPlayer player) implements ForwardingAudience {
    @Override
    public Iterable<? extends Audience> audiences() {
        return Collections.singleton(audience);
    }

    public boolean isPlayer(OffsetPlayer player) {
        return this.player != null && this.player.equals(player);
    }
}
