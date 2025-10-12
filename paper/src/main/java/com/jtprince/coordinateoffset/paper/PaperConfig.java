package com.jtprince.coordinateoffset.paper;

import de.exlll.configlib.Comment;
import de.exlll.configlib.ConfigLib;
import de.exlll.configlib.Configuration;
import de.exlll.configlib.YamlConfigurationProperties;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

@NullMarked
@Configuration
public class PaperConfig {
    static YamlConfigurationProperties properties =
        ConfigLib.BUKKIT_DEFAULT_PROPERTIES.toBuilder()
            // TODO: Add version to comment, mention comments being overwritten on run
            .header("""
                    CoordinateOffset Configuration File
                    See https://github.com/joshuaprince/CoordinateOffset/wiki/Configuration-Guide
                    """)
            .build();

    @Comment({
        "Specify the method by which coordinate offsets will be calculated.",
        "Options are any key under `offsetProviders` below (e.g. constant, random...)"
    })
    String defaultOffsetProvider = "random";

    @Configuration
    public static class OffsetProviderOverride {
        String provider = "";
        @Nullable String world;
        @Nullable String permission;
        @Nullable String playerUuid;
    }
    @Comment({
        "",
        "List of overrides to the default offset provider. The first item has the",
        "  highest priority. `provider` is a required key; optional keys are",
        "  `world`, `playerUuid`, and `permission` which, if present, must ALL match",
        "  for the override to apply. Example:",
        "offsetProviderOverrides:",
        " - provider: constant",
        "   world: world_nether",
        "   permission: coordinateoffset.provider.my_custom_permission",
        " - provider: zeroAtLocation",
        "   playerUuid: 00000000-0000-0000-0000-000000000000"
    })
    List<OffsetProviderOverride> offsetProviderOverrides = List.of();

    // TODO: Put dynamic serializers for all offset providers here

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

    @Comment({
        "",
        "If true, players with the `coordinateoffset.bypass` permission will bypass",
        "  all providers and see their real coordinates."
    })
    boolean bypassByPermission = false;

    @Comment({
        "",
        "Wait for players to be near the world border to send border packets.",
        "  Disabling this fixes moving borders, but may leak coordinates - see",
        "  https://github.com/joshuaprince/CoordinateOffset/wiki/Implications-and-Limitations#world-border"
    })
    boolean obfuscateWorldBorder = true;

    @Comment({
        "",
        "Enable a log message when a player's offset changes."
    })
    boolean verbose = false;

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
}
