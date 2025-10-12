package com.jtprince.coordinateoffset.provider.util;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * List of world alignment configurations from config.yml, which are of the format:
 * <br><br>
 * {@code world1:world2:scaleFactor}
 * <br><br>
 * A world alignment configuration indicates that a coordinate offset provider should link the offsets of two worlds
 * so that they behave in a related manner. For example, a Vanilla server would likely use a configuration of:
 * <br><br>
 * {@code world:world_nether:8}
 * <br><br>
 * Then, any offsets provided in {@code world} would be scaled down by a factor of 8 and reused in {@code world_nether}.
 */
public class WorldAlignmentConfig {
    private record Alignment(String greaterWorldName, String lesserWorldName, int scaleShift) {}
    public record QueryResult(String targetWorldName, int rightShiftAmount) {}

    private final List<Alignment> alignments;

    private WorldAlignmentConfig(List<Alignment> alignments) {
        this.alignments = alignments;
    }

    public static WorldAlignmentConfig fromConfig(List<String> configStringList) {
        List<Alignment> alignments = new ArrayList<>();
        for (String str : configStringList) {
            String[] tokens = str.split(":");
            Alignment alignment;
            switch (tokens.length) {
                case 2 -> alignment = new Alignment(tokens[0], tokens[1], 0);
                case 3 -> {
                    int scaleFactor = Integer.parseInt(tokens[2]);
                    Integer shift = logBaseTwoIntOrNull(scaleFactor);
                    if (shift == null) {
                        throw new IllegalArgumentException("Scale factor " + scaleFactor + " in world alignment " + str + " must be a power of 2!");
                    }
                    alignment = new Alignment(tokens[0], tokens[1], shift);
                }
                default -> throw new IllegalArgumentException("Badly formatted world alignment: " + str);
            }
            alignments.add(alignment);
        }
        return new WorldAlignmentConfig(alignments);
    }

    /**
     * Determine if a world with the given name needs to be aligned with any other world, and if so, the amount that
     * the offset for the returned world needs to be right-shifted to properly scale the offset on the input world.
     * Example: In a Vanilla setup,
     * <pre>
     *     "world" should reuse the offset from "world_nether", but multiply by 8
     *     findAlignment(world) => (world_nether, -3)
     *     "world_nether" should reuse the offset from "world", but divide by 8
     *     findAlignment(world) => (world_nether, 3)
     * </pre>
     *
     * @param worldName The name of a world whose offset we are determining.
     * @return A {@link QueryResult} that contains the aligned world's name and the scaling factor to apply to the
     *         aligned world's offset to determine an offset for <code>worldName</code>. <code>null</code> if there
     *         is no aligned world.
     */
    public @Nullable QueryResult findAlignment(String worldName) {
        for (Alignment alignment : alignments) {
            if (alignment.greaterWorldName.equals(worldName)) return new QueryResult(alignment.lesserWorldName, -alignment.scaleShift);
            if (alignment.lesserWorldName.equals(worldName)) return new QueryResult(alignment.greaterWorldName, alignment.scaleShift);
        }
        return null;
    }

    /**
     * Determine the greatest factor by which we might have to scale a world's offset down.
     * For example, in the standard {@code world:world_nether:8}, we might have to scale the offset for {@code world}
     * down by 8, so this function would return {@code log_2(8) == 3}. Since {@code world_nether} never has to be scaled
     * down this would return 0.
     * @param worldName A world name.
     * @return A non-negative integer representing a shift amount.
     */
    public int greatestPossibleRightShiftForWorld(String worldName) {
        return alignments.stream()
                .filter(a -> a.greaterWorldName.equals(worldName))
                .max(Comparator.comparing(a -> a.scaleShift))
                .map(a -> a.scaleShift)
                .orElse(0);
    }

    /**
     * Logarithm base 2 of the number, but only if it is an integer.
     * <pre>
     *     0   -> null
     *     1   -> 0
     *     2   -> 1
     *     3   -> null
     *     4   -> 2
     *     5-7 -> null
     *     8   -> 3
     * </pre>
     */
    private static @Nullable Integer logBaseTwoIntOrNull(int x) {
        if ((x == 0) || ((x & (x - 1)) != 0)) return null;

        int shifts = 0;
        while (x != 0) {
            x >>= 1;
            shifts++;
        }
        return shifts - 1;
    }
}
