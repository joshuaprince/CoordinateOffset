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
        "############################################################################ #", // keep this header at the top
        "################### General CoordinateOffset Configuration ################# #",
        "############################################################################ #",
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
        "If true, players with the `coordinateoffset.bypass` permission will always",
        "  see their real coordinates (no offsets). Disable this to test the plugin."
    })
    boolean bypassByPermission = true;
    public boolean getBypassByPermission() {
        return bypassByPermission;
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

    @Nullable Boolean allowUnsafeResetOnDistantTeleport = null;
    public boolean getUnsafeResetOnDistantTeleport() {
        return allowUnsafeResetOnDistantTeleport != null && allowUnsafeResetOnDistantTeleport;
    }
}
