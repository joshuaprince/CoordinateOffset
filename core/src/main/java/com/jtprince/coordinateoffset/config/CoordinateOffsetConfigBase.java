package com.jtprince.coordinateoffset.config;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import de.exlll.configlib.SerializeWith;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.SequencedMap;

@NullMarked
@Configuration
public class CoordinateOffsetConfigBase implements CoordinateOffsetConfig {
    @Comment("Do not change this.")
    @Nullable Integer configVersion = null;
    public @Nullable Integer getConfigVersion() {
        return configVersion;
    }

    @Comment({
        "",
        "############################################################################ #", // keep this header at the top
        "################### General CoordinateOffset Configuration ################# #",
        "############################################################################ #",
        "",
        "If true, players with the `coordinateoffset.bypass` permission will always",
        "  see their real coordinates (no offsets). Disable this to test the plugin."
    })
    boolean bypassByPermission = true;
    public boolean getBypassByPermission() {
        return bypassByPermission;
    }

    @Configuration
    public static class FixCollision {
        boolean bamboo = true;
        boolean dripstone = true;
    }
    @Comment({
        "",
        "Disable server-side collision checks for the listed blocks.",
        "  If collision checks are left enabled, movement near these blocks will be",
        "  extremely glitchy for all players with an offset applied.",
        "  More info: https://github.com/joshuaprince/CoordinateOffset/issues/8",
        "Note: Requires a server restart for changes to take effect."
    })
    FixCollision fixCollision = new FixCollision();

    public boolean getFixCollisionBamboo() {
        return fixCollision.bamboo;
    }
    public boolean getFixCollisionDripstone() {
        return fixCollision.dripstone;
    }

    @Comment({
        "",
        "Don't send world border packets to players who are far from the world border.",
        "  Disable if your world border moves, but beware that it may leak coordinates:",
        "  https://github.com/joshuaprince/CoordinateOffset/wiki/Implications-and-Limitations#world-border"
    })
    boolean obfuscateWorldBorder = true;
    public boolean getObfuscateWorldBorder() {
        return obfuscateWorldBorder;
    }

    @Comment({
        "",
        "Don't send any \"debug\" packets to players with an applied offset.",
        "  Debug information reveals real coordinates if this is disabled.",
        "  More info: https://minecraft.wiki/w/Debug_property"
    })
    boolean obfuscateDebugPropertySubscriptions = true;
    public boolean getObfuscateDebugPropertySubscriptions() {
        return obfuscateDebugPropertySubscriptions;
    }

    @Comment({
        "",
        "Enable a log message when a player's offset changes."
    })
    boolean verbose = false;
    public boolean getVerbose() {
        return verbose;
    }

    @Comment({
        "",
        "Round generated offsets to the nearest multiple of this number of blocks.",
        "  Must be at least 16 and a power of 2 (16, 32, 64, 128, etc.).",
        "  \"auto\" selects the lowest value compatible with other installed plugins,",
        "  e.g. the Distant Horizons plugin requires 64+ for offsets to be compatible.",
    })
    @SerializeWith(serializer = OffsetMultipleConfig.Serializer.class)
    public OffsetMultipleConfig offsetsAreMultiplesOfBlocks = OffsetMultipleConfig.AUTO;
    public int getOffsetsAreMultiplesOfBlocks() {
        return offsetsAreMultiplesOfBlocks.getMultiple();
    }

    @Comment({
        "",
        "Custom scaling for coordinates between worlds. Default overworld/end scale is",
        "  1.0 and default nether scale is 8.0. Offsets are divided by this value."
    })
    @Nullable SequencedMap<String, Double> worldCoordinateScaleOverrides = null; // Not in default config
    public SequencedMap<String, Double> getWorldCoordinateScaleOverrides() {
        return Objects.requireNonNullElseGet(worldCoordinateScaleOverrides, LinkedHashMap::new);
    }
}
