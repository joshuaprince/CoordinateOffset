package com.jtprince.coordinateoffset.config;

import com.jtprince.coordinateoffset.provider.DefaultOffsetProviders;
import com.jtprince.coordinateoffset.provider.OffsetProvider;
import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import de.exlll.configlib.SerializeWith;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.Objects;
import java.util.SequencedMap;

@NullMarked
@Configuration
public class CoordinateOffsetConfigFull extends CoordinateOffsetConfigBase implements CoordinateOffsetProviderConfig {
    @Comment({
        "",
        "Specify the method by which coordinate offsets will be calculated.",
        "Options are any key under `offsetProviders` below (e.g. constant, random...)"
    })
    String defaultOffsetProvider = "random";
    public OffsetProvider getDefaultOffsetProviderConfig() {
        return Objects.requireNonNull(offsetProviders.get(defaultOffsetProvider));
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
    public SequencedMap<String, OffsetProvider> getAllOffsetProviderConfigs() {
        return offsetProviders;
    }
}
