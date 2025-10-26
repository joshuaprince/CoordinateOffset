package com.jtprince.coordinateoffset.provider.util;

import com.jtprince.coordinateoffset.CoordinateOffsetCore;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.adapter.OffsetWorld;
import com.jtprince.coordinateoffset.provider.OffsetProvider;

public class CoordinateScaleUtils {
    public static Offset scaleVerbosely(
        Offset offset,
        OffsetWorld world,
        OffsetProvider provider,
        String adjective
    ) {
        if (CoordinateOffsetCore.get().getConfig().getVerbose() && world.getCoordinateScale() != 1.0) {
            CoordinateOffsetCore.get().getLogger().info("Provider \"" + provider.name + "\": Scaling "
                + adjective + " " + offset + " by " + world.getCoordinateScale() + " to match coordinate scale "
                + "of world \"" + world.getName() + "\"");
        }
        return offset.scaleDownBy(world.getCoordinateScale());
    }
}
