package com.jtprince.coordinateoffset.config;

import com.jtprince.coordinateoffset.provider.DefaultOffsetProviders;
import com.jtprince.coordinateoffset.provider.OffsetProvider;
import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import de.exlll.configlib.SerializeWith;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.SequencedMap;

@NullMarked
@Configuration
public class CoordinateOffsetConfigImpl implements CoordinateOffsetConfig {
    @Comment({
        "Specify the method by which coordinate offsets will be calculated.",
        "Options are any key under `offsetProviders` below (e.g. constant, random...)"
    })
    String defaultOffsetProvider = "random";
    @Override
    public OffsetProvider getDefaultOffsetProviderConfig() {
        return offsetProviders.get(defaultOffsetProvider);
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
    List<OffsetProviderOverrideConfig> offsetProviderOverrides = List.of();
    @Override
    public List<OffsetProviderOverrideConfig> getOffsetProviderOverrides() {
        return offsetProviderOverrides;
    }

    @Comment({
        "",
        "Configuration for all available offset providers. Each provider must have a",
        "  unique key (e.g. \"constant\"), which is used in `defaultOffsetProvider` and",
        "  `offsetProviderOverrides`. You may add your own keys to define as many",
        "  providers as you need.",
        "See the configuration guide for details about which options are available for",
        "  each provider class.",
        "https://github.com/joshuaprince/CoordinateOffset/wiki/Configuration-Guide"
    })
    @SerializeWith(serializer = OffsetProviderListSerializer.class)
    SequencedMap<String, OffsetProvider> offsetProviders = DefaultOffsetProviders.PROVIDERS;
    @Override
    public SequencedMap<String, OffsetProvider> getAllOffsetProviderConfigs() {
        return offsetProviders;
    }

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
    @Override
    public boolean getFixCollisionBamboo() {
        return fixCollision.bamboo;
    }
    @Override
    public boolean getFixCollisionDripstone() {
        return fixCollision.dripstone;
    }

    @Comment({
        "",
        "If true, players with the `coordinateoffset.bypass` permission will bypass",
        "  all providers and see their real coordinates."
    })
    boolean bypassByPermission = false;
    @Override
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
    @Override
    public boolean getObfuscateWorldBorder() {
        return obfuscateWorldBorder;
    }

    @Comment({
        "",
        "Enable a log message when a player's offset changes."
    })
    boolean verbose = false;
    @Override
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
    @Override
    public boolean getDebugEnable() {
        return debug.enable;
    }
    @Override
    public int getDebugPacketHistorySize() {
        return Math.max(1, debug.packetHistorySize);
    }

    @Nullable Boolean unsafeResetOnDistantTeleport = null;
    @Override
    public boolean getUnsafeResetOnDistantTeleport() {
        return unsafeResetOnDistantTeleport != null && unsafeResetOnDistantTeleport;
    }
}
