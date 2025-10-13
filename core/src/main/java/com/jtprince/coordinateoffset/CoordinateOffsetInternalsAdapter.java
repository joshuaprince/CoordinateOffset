package com.jtprince.coordinateoffset;

import com.jtprince.coordinateoffset.adapter.CoordinateOffsetAdapter;
import com.jtprince.coordinateoffset.provider.util.PlayerOffsetPersistence;

public interface CoordinateOffsetInternalsAdapter extends CoordinateOffsetAdapter {
    PlayerOffsetPersistence getPlayerOffsetPersistence();
}
