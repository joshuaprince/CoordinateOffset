package com.jtprince.coordinateoffset;

public class CoordinateOffsetPlatform {
    private static CoordinateOffset instance;
    public static void setInstance(CoordinateOffset instance) {
        if (CoordinateOffsetPlatform.instance != null) {
            throw new IllegalStateException("Instance already set");
        }
        CoordinateOffsetPlatform.instance = instance;
    }
    public static CoordinateOffset getInstance() {
        return instance;
    }
}
