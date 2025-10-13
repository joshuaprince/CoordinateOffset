package com.jtprince.coordinateoffset.adapter;

/**
 * Adapter interface representing a location in a Minecraft world with X, Y, Z coordinates.
 */
public interface OffsetLocation {
    String getWorldName();
    double getX();
    double getY();
    double getZ();
}
