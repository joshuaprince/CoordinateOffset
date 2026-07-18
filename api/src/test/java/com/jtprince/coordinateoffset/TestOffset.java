package com.jtprince.coordinateoffset;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestOffset {
    @Test
    void testNonAlignedThrowsException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Offset.fixed(-64, 0, 24));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Offset.fixed(100, 0, 32));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Offset.fixed(-1000, 0, -100));
        Assertions.assertDoesNotThrow(() -> Offset.fixed(0, 37, 0));
        Assertions.assertDoesNotThrow(() -> Offset.fixed(0, -200, 0));
    }

    @Test
    void testAlignComponent() {
        Assertions.assertEquals(0, Offset.alignComponent(0, 0));
        Assertions.assertEquals(16, Offset.alignComponent(15, 0)); // 0, 16, 32...

        Assertions.assertEquals(0, Offset.alignComponent(15, 1)); // 0, 32, 64...
        Assertions.assertEquals(0, Offset.alignComponent(-16, 1)); // -32, 0, 32...
        Assertions.assertEquals(32, Offset.alignComponent(16, 1)); // 0, 32, 64...

        Assertions.assertEquals(0, Offset.alignComponent(1, 3)); // 0, 128, 256...
        Assertions.assertEquals(0, Offset.alignComponent(63, 3)); // 0, 128, 256...
        Assertions.assertEquals(128, Offset.alignComponent(64, 3)); // 0, 128, 256...
        Assertions.assertEquals(128, Offset.alignComponent(65, 3)); // 0, 128, 256...
        Assertions.assertEquals(-128, Offset.alignComponent(-65, 3)); // -128, 0, 128...
        Assertions.assertEquals(0, Offset.alignComponent(-64, 3)); // -128, 0, 128...
        Assertions.assertEquals(0, Offset.alignComponent(-63, 3)); // -128, 0, 128...

        Assertions.assertEquals(16, Offset.alignComponent(17, -1)); // same as 0
    }

    @Test
    void testAlignToMultipleChunks() {
        // Align to 16 chunks (nearest 256 blocks)
        Assertions.assertEquals(new ScalableOffset(0, 0, 0), Offset.align(-65, 65, 4));
        Assertions.assertEquals(new ScalableOffset(0, 0, 0), Offset.align(-128, 127, 4));
        Assertions.assertEquals(new ScalableOffset(-256, 0, 256), Offset.align(-129, 128, 4));
    }

    @Test
    void testChunkValues() {
        Assertions.assertEquals(5, Offset.fixed(80, 0, 0).chunkX());
        Assertions.assertEquals(-3, Offset.fixed(0, 0, -48).chunkZ());
    }

    @Test
    void testScale() {
        Assertions.assertEquals(new FixedOffset(64, 0, -128), Offset.scalable(16, 0, -32).scaleDownBy(0.25));
        Assertions.assertEquals(new FixedOffset(-48, 0, -128), Offset.scalable(-48, 0, -128).scaleDownBy(1.0));

        // Clean scaling (dividing inputs by 8 still results in a multiple of 16)
        Assertions.assertEquals(new FixedOffset(96, 0, -176), Offset.scalable(768, 0, -1408).scaleDownBy(8.0));
        // Truncated scaling (dividing inputs by 8 does NOT result in a multiple of 16, so they must be aligned)
        Assertions.assertEquals(new FixedOffset(-48, 0, 16), Offset.scalable(-432, 0, 144).scaleDownBy(8.0));
        Assertions.assertEquals(new FixedOffset(-64, 0, 32), Offset.scalable(-464, 0, 192).scaleDownBy(8.0));
    }

    @Test
    void testYOffset() {
        FixedOffset withY = Offset.fixed(0, 128, 0);
        Assertions.assertEquals(128, withY.y());

        // Y is not scaled by world coordinate scale
        ScalableOffset scalable = Offset.scalable(0, 64, 0);
        Assertions.assertEquals(64, scalable.scaleDownBy(8.0).y());
        Assertions.assertEquals(64, scalable.scaleDownBy(1.0).y());

        Assertions.assertEquals(-128, Offset.fixed(0, 128, 0).negate().y());
        Assertions.assertEquals(-64, Offset.scalable(0, 64, 0).negate().y());

        Assertions.assertFalse(Offset.fixed(0, 1, 0).isZero());
        Assertions.assertTrue(Offset.fixed(0, 0, 0).isZero());
    }
}
