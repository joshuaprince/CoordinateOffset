package com.jtprince.coordinateoffset;

import com.jtprince.coordinateoffset.paper.adapter.PaperAdapter;
import com.jtprince.coordinateoffset.paper.adapter.PaperLocation;
import org.bukkit.Location;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestPaperOffset {
    @BeforeAll
    static void setUp() {
        CoordinateOffsetCore.bootstrapForTests(new PaperAdapter(null));
    }

    @Test
    void testApplyBukkitLocation() {
        // Test PaperLocation::apply
        Offset offset = new Offset(-128, 48);
        PaperLocation loc = new PaperLocation(new Location(null, 10.7, 48.2, -233.5));

        Location expected = new Location(null, 138.7, 48.2, -281.5);
        Location actual = (Location) loc.apply(offset).getPlatformLocationObject();

        assertBukkitLocationsApproximatelyEqual(expected, actual);
    }

    @Test
    void testUnApplyBukkitLocation() {
        // Test PaperLocation::unapply
        Offset offset = new Offset(-128, 48);
        PaperLocation loc = new PaperLocation(new Location(null, 138.7, 48.2, -281.5));

        Location expected = new Location(null, 10.7, 48.2, -233.5);
        Location actual = (Location) loc.unapply(offset).getPlatformLocationObject();

        assertBukkitLocationsApproximatelyEqual(expected, actual);
    }

    @Test
    void testOffsetApplyBukkitLocation() {
        // Test Offset::apply with a Bukkit Location
        Offset offset = new Offset(-128, 48);
        Location loc = new Location(null, 10.7, 48.2, -233.5);

        Location expected = new Location(null, 138.7, 48.2, -281.5);
        Location actual = offset.apply(loc);

        assertBukkitLocationsApproximatelyEqual(expected, actual);
    }

    @Test
    void testOffsetApplyAdaptedBukkitLocation() {
        // Test Offset::apply with a Bukkit Location adapted to an OffsetLocation
        Offset offset = new Offset(-128, 48);
        Location loc = new Location(null, 10.7, 48.2, -233.5);

        PaperLocation expected = new PaperLocation(new Location(null, 138.7, 48.2, -281.5));
        PaperLocation actual = offset.apply(new PaperLocation(loc));

        assertOffsetLocationsApproximatelyEqual(expected, actual);
    }

    @Test
    void testOffsetUnapplyBukkitLocation() {
        // Test Offset::unapply with a Bukkit Location
        Offset offset = new Offset(-128, 48);
        Location loc = new Location(null, 138.7, 48.2, -281.5);

        Location expected = new Location(null, 10.7, 48.2, -233.5);
        Location actual = offset.unapply(loc);

        assertBukkitLocationsApproximatelyEqual(expected, actual);
    }

    @Test
    void testOffsetUnapplyAdaptedBukkitLocation() {
        // Test Offset::unapply with a Bukkit Location adapted to an OffsetLocation
        Offset offset = new Offset(-128, 48);
        Location loc = new Location(null, 138.7, 48.2, -281.5);

        PaperLocation expected = new PaperLocation(new Location(null, 10.7, 48.2, -233.5));
        PaperLocation actual = offset.unapply(new PaperLocation(loc));

        assertOffsetLocationsApproximatelyEqual(expected, actual);
    }

    private void assertBukkitLocationsApproximatelyEqual(Location expected, Location actual) {
        // Deal with floating-point imprecision when we add/subtract for applying offsets
        Assertions.assertEquals(expected.getWorld(), actual.getWorld());
        Assertions.assertEquals(expected.getX(), actual.getX(), 0.001);
        Assertions.assertEquals(expected.getY(), actual.getY(), 0.001);
        Assertions.assertEquals(expected.getZ(), actual.getZ(), 0.001);
    }

    private void assertOffsetLocationsApproximatelyEqual(PaperLocation expected, PaperLocation actual) {
        // Deal with floating-point imprecision when we add/subtract for applying offsets
        Assertions.assertEquals(expected.getWorldName(), actual.getWorldName());
        Assertions.assertEquals(expected.getX(), actual.getX(), 0.001);
        Assertions.assertEquals(expected.getY(), actual.getY(), 0.001);
        Assertions.assertEquals(expected.getZ(), actual.getZ(), 0.001);
    }
}
