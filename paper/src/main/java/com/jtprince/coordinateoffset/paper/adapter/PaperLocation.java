package com.jtprince.coordinateoffset.paper.adapter;

import com.jtprince.coordinateoffset.FixedOffset;
import com.jtprince.coordinateoffset.adapter.OffsetLocation;
import com.jtprince.coordinateoffset.adapter.OffsetWorld;
import org.bukkit.Location;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class PaperLocation implements OffsetLocation {
    private final Location location;
    public PaperLocation(Location location) {
        this.location = location;
    }

    @Override
    public @Nullable OffsetWorld getWorld() {
        if (location.getWorld() == null) return null;
        return new PaperWorld(location.getWorld().getUID());
    }

    @Override
    public double getX() {
        return location.getX();
    }

    @Override
    public double getY() {
        return location.getY();
    }

    @Override
    public double getZ() {
        return location.getZ();
    }

    @Override
    public OffsetLocation apply(FixedOffset offset) {
        return new PaperLocation(location.clone().subtract(offset.x(), offset.y(), offset.z()));
    }

    @Override
    public OffsetLocation unapply(FixedOffset offset) {
        return new PaperLocation(location.clone().add(offset.x(), offset.y(), offset.z()));
    }

    @Override
    public Location getPlatformLocationObject() {
        return location;
    }

    @Override
    public String toString() {
        return location.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof PaperLocation p) && location.equals(p.location);
    }
}
