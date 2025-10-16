package com.jtprince.coordinateoffset.paper.adapter;

import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.adapter.OffsetLocation;
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
    @Nullable public String getWorldName() {
        if (location.getWorld() == null) return null;
        return location.getWorld().getName();
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
    public OffsetLocation apply(Offset offset) {
        return new PaperLocation(location.clone().subtract(offset.x(), 0, offset.z()));
    }

    @Override
    public OffsetLocation unapply(Offset offset) {
        return new PaperLocation(location.clone().add(offset.x(), 0, offset.z()));
    }

    @Override
    public Object getPlatformLocationObject() {
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
