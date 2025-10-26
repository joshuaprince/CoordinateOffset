package com.jtprince.coordinateoffset.paper.adapter;

import com.jtprince.coordinateoffset.adapter.OffsetWorld;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;
import java.util.UUID;

@NullMarked
public class PaperWorld implements OffsetWorld {
    UUID uuid;
    public PaperWorld(UUID worldUuid) {
        this.uuid = worldUuid;
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public String getName() {
        return getPlatformPlayerObject().getName();
    }

    @Override
    public String getKey() {
        return getPlatformPlayerObject().getKey().asString();
    }

    @Override
    public Double getCoordinateScale() {
        return getPlatformPlayerObject().getCoordinateScale();
    }

    @Override
    public World getPlatformPlayerObject() {
        return Objects.requireNonNull(Bukkit.getWorld(uuid));
    }

    @Override
    public String toString() {
        return getPlatformPlayerObject().toString();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof PaperWorld w) && uuid.equals(w.uuid);
    }
}
