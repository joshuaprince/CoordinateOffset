package com.jtprince.coordinateoffset;

import org.bukkit.Location;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestOffset {
    @Test
    void testNonAlignedThrowsException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Offset(-64, 24));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Offset(100, 32));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Offset(-1000, -100));
    }

    @Test
    void testAlignToSingleChunk() {
        Assertions.assertEquals(new Offset(64, -16), Offset.align(70, -12));
    }

    @Test
    void testAlignRoundsToNearest() {
        Assertions.assertEquals(new Offset(16, -16), Offset.align(23, -9));
        Assertions.assertEquals(new Offset(32, 0), Offset.align(24, -8));
    }

    @Test
    void testAlignToMultipleChunks() {
        // Align to 8 chunks (nearest 128 blocks)
        Assertions.assertEquals(new Offset(0, 128), Offset.align(-64, 64, 3));
        Assertions.assertEquals(new Offset(-128, 128), Offset.align(-65, 65, 3));
        // Align to 16 chunks (nearest 256 blocks)
        Assertions.assertEquals(new Offset(0, 0), Offset.align(-65, 65, 4));
        Assertions.assertEquals(new Offset(0, 0), Offset.align(-128, 127, 4));
        Assertions.assertEquals(new Offset(-256, 256), Offset.align(-129, 128, 4));
    }

    @Test
    void testChunkValues() {
        Assertions.assertEquals(5, new Offset(80, 0).chunkX());
        Assertions.assertEquals(-3, new Offset(0, -48).chunkZ());
    }

    @Test
    void testScale() {
        Assertions.assertEquals(new Offset(64, -128), new Offset(16, -32).scale(-2));
        Assertions.assertEquals(new Offset(-48, -128), new Offset(-48, -128).scale(0));

        // Clean scaling (dividing inputs by 8 still results in a multiple of 16)
        Assertions.assertEquals(new Offset(96, -176), new Offset(768, -1408).scale(3));
        // Truncated scaling (dividing inputs by 8 does NOT result in a multiple of 16, so they must be aligned)
        Assertions.assertEquals(new Offset(-48, 16), new Offset(-432, 144).scale(3));
        Assertions.assertEquals(new Offset(-64, 32), new Offset(-464, 192).scale(3));
    }

    @Test
    void testApplyBukkitLocation() {
        Offset offset = new Offset(-128, 48);
        Assertions.assertNull(offset.apply((Location) null));
        assertLocationsApproximatelyEqual(new Location(null, 138.7, 48.2, -281.5),
                offset.apply(new Location(null, 10.7, 48.2, -233.5)));
    }

    @Test
    void testUnApplyBukkitLocation() {
        Offset offset = new Offset(-128, 48);
        Assertions.assertNull(offset.unapply((Location) null));
        assertLocationsApproximatelyEqual(new Location(null, 10.7, 48.2, -233.5),
                offset.unapply(new Location(null, 138.7, 48.2, -281.5)));
    }

    private void assertLocationsApproximatelyEqual(Location expected, Location actual) {
        // Deal with floating-point imprecision when we add/subtract for applying offsets
        Assertions.assertEquals(expected.getWorld(), actual.getWorld());
        Assertions.assertEquals(expected.getX(), actual.getX(), 0.001);
        Assertions.assertEquals(expected.getY(), actual.getY(), 0.001);
        Assertions.assertEquals(expected.getZ(), actual.getZ(), 0.001);
    }
}
