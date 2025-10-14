package com.jtprince.coordinateoffset.paper.adapter;

import com.jtprince.coordinateoffset.adapter.OffsetLocation;
import org.bukkit.Location;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class PaperLocation implements OffsetLocation {
    private final Location location;
    public PaperLocation(Location location) {
        this.location = location;
    }

    @Override
    public String getWorldName() {
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
