package com.jtprince.coordinateoffset.paper;

import com.jtprince.coordinateoffset.adapter.OffsetPlayer;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

@NullMarked
public class PaperOffsetPlayer implements OffsetPlayer {
    private final Player player;
    public PaperOffsetPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public UUID getUuid() {
        return player.getUniqueId();
    }

    @Override
    public String getName() {
        return player.getName();
    }
}
