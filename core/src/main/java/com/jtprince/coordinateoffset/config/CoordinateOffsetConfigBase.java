package com.jtprince.coordinateoffset.config;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@Configuration
public class CoordinateOffsetConfigBase implements CoordinateOffsetConfig {
    @Comment("Do not change this.")
    @Nullable Integer configVersion = null;

    @Configuration
    public static class FixCollision {
        boolean bamboo = true;
        boolean dripstone = true;
    }
    @Comment({
        "",
        "Coordinates are used to determine how to shift these blocks slightly. That",
        "  means that if the client and server disagree about coordinates, they also",
        "  disagree about how to shift these blocks, so movement is glitchy near these",
        "  blocks. These settings completely disable collisions for these blocks.",
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
        "If true, players with the `coordinateoffset.bypass` permission will bypass",
        "  all providers and see their real coordinates."
    })
    boolean bypassByPermission = false;
    public boolean getBypassByPermission() {
        return bypassByPermission;
    }

    @Comment({
        "",
        "Wait for players to be near the world border to send border packets.",
        "  Disabling this fixes moving borders, but may leak coordinates - see",
        "  https://github.com/joshuaprince/CoordinateOffset/wiki/Implications-and-Limitations#world-border"
    })
    boolean obfuscateWorldBorder = true;
    public boolean getObfuscateWorldBorder() {
        return obfuscateWorldBorder;
    }

    @Comment({
        "",
        "Hide all \"debug\" information from players with a nonzero offset.",
        "  Debug information reveals real coordinates if obfuscation is disabled.",
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

    @Configuration
    public static class DebugOptions {
        boolean enable = false;
        int packetHistorySize = 8;
    }
    @Comment({
        "",
        "Enable additional logging when errors occur in the plugin.",
        "  Do not enable in production!"
    })
    DebugOptions debug = new DebugOptions();
    public boolean getDebugEnable() {
        return debug.enable;
    }
    public int getDebugPacketHistorySize() {
        return Math.max(1, debug.packetHistorySize);
    }

    @Nullable Boolean unsafeResetOnDistantTeleport = null;
    public boolean getUnsafeResetOnDistantTeleport() {
        return unsafeResetOnDistantTeleport != null && unsafeResetOnDistantTeleport;
    }
}
