package com.jtprince.coordinateoffset;

import java.util.logging.Logger;

public interface CoordinateOffset {
    Logger getLogger();

    static CoordinateOffset getInstance() {
        return CoordinateOffsetPlatform.getInstance();
    }
}
