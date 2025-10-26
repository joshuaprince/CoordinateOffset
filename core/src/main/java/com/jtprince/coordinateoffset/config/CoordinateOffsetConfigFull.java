package com.jtprince.coordinateoffset.config;

import com.jtprince.coordinateoffset.CoordinateOffsetCore;
import com.jtprince.coordinateoffset.provider.DefaultOffsetProviders;
import com.jtprince.coordinateoffset.provider.OffsetProvider;
import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import de.exlll.configlib.SerializeWith;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.Objects;
import java.util.SequencedMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@NullMarked
@Configuration
public class CoordinateOffsetConfigFull extends CoordinateOffsetConfigBase implements CoordinateOffsetProviderConfig {
    @Comment({
        "",
        "############################################################################ #", // keep this header at the top
        "####################### Offset Provider Configuration ###################### #",
        "############################################################################ #",
        "",
        "Specify the method used to apply coordinate offsets to players.",
        "Options are any key under `offsetProviders` below (e.g. constant, random...)"
    })
    String defaultOffsetProvider = "random";
    public OffsetProvider getDefaultOffsetProviderConfig() {
        return Objects.requireNonNull(offsetProviders.get(defaultOffsetProvider));
    }

    @Comment({
        "",
        "List of overrides to the default offset provider. The first item has the",
        "  highest priority. `provider` is a required key. Optional keys are",
        "  `world`, `player`, and `permission` which, if present, must ALL match",
        "  for the override to apply. Example:",
        "offsetProviderOverrides:",
        " - provider: constant",
        "   world: world_nether",
        "   permission: coordinateoffset.provider.my_custom_permission",
        " - provider: zeroAtLocation",
        "   player: jeb_"
    })
    List<OffsetProviderOverrideConfigImpl> offsetProviderOverrides = List.of();
    public List<OffsetProviderOverrideConfig> getOffsetProviderOverrides() {
        return offsetProviderOverrides.stream()
            .filter(o -> o.validate(offsetProviders, false))
            .collect(Collectors.toUnmodifiableList());
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

    /**
     * Validate that the fully-loaded configuration is valid.
     *
     * @return true if the configuration is acceptable to proceed, false if there is a problem that prevents
     *         the plugin from functioning correctly.
     */
    public boolean validateBaseAndFullConfig() {
        Logger logger = CoordinateOffsetCore.get().getLogger();

        // Default provider must exist
        try {
            getDefaultOffsetProviderConfig();
        } catch (NullPointerException e) {
            logger.severe("Failed to load offset providers from config.");
            logger.severe("An offset provider named \"" + defaultOffsetProvider + "\" was configured as the default provider, but no such provider exists.");
            logger.severe("Check your configuration and make sure that:");
            logger.severe("  1) The provider name is spelled correctly.");
            logger.severe("  2) A provider whose name matches exactly is defined in the 'offsetProviders' section.");
            logger.severe("  3) There are no other errors in the log that may indicate why the provider failed to load.");
            logger.severe("If the problem persists, please contact the plugin author for assistance.");
            return false;
        }

        // Override rules must be valid, but not fatal
        for (OffsetProviderOverrideConfigImpl override : offsetProviderOverrides) {
            override.validate(offsetProviders, true);
            // No early return; they'll be excluded in calls to getOffsetProviderOverrides
        }

        return true;
    }
}
