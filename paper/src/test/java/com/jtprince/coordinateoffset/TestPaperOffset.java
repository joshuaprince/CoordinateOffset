package com.jtprince.coordinateoffset;

import com.jtprince.coordinateoffset.paper.PaperOffset;
import org.bukkit.Location;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestPaperOffset {
    @Test
    void testApplyBukkitLocation() {
        Offset offset = new Offset(-128, 48);
        assertLocationsApproximatelyEqual(new Location(null, 138.7, 48.2, -281.5),
                PaperOffset.apply(new Location(null, 10.7, 48.2, -233.5), offset));
    }

    @Test
    void testUnApplyBukkitLocation() {
        Offset offset = new Offset(-128, 48);
        assertLocationsApproximatelyEqual(new Location(null, 10.7, 48.2, -233.5),
                PaperOffset.unapply(new Location(null, 138.7, 48.2, -281.5), offset));
    }

    private void assertLocationsApproximatelyEqual(Location expected, Location actual) {
        // Deal with floating-point imprecision when we add/subtract for applying offsets
        Assertions.assertEquals(expected.getWorld(), actual.getWorld());
        Assertions.assertEquals(expected.getX(), actual.getX(), 0.001);
        Assertions.assertEquals(expected.getY(), actual.getY(), 0.001);
        Assertions.assertEquals(expected.getZ(), actual.getZ(), 0.001);
    }
}
