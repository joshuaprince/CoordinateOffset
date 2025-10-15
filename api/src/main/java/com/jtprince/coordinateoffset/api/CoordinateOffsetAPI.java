package com.jtprince.coordinateoffset.api;

import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.adapter.OffsetPlayer;
import com.jtprince.coordinateoffset.config.CoordinateOffsetConfig;
import com.jtprince.coordinateoffset.config.CoordinateOffsetProviderConfig;
import com.jtprince.coordinateoffset.provider.OffsetProvider;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * API for the CoordinateOffset plugin.
 *
 * <p>External plugins may get a singleton instance of this API via {@link CoordinateOffset#get()}.</p>
 */
@NullMarked
public interface CoordinateOffsetAPI {
    /**
     * Get the currently active coordinate {@link Offset} for a player.
     *
     * <p>Offsets are <b>subtracted</b> from a player's real coordinates to determine the coordinates they see.
     * As an example, a player might have a coordinate offset of <code>(128, 128)</code>. This would mean that the
     * player standing at <code>(128, 128)</code> sees that they are standing at <code>(0, 0)</code> in the "F3"
     * menu.</p> Similarly, if the player moves to the world's origin <code>(0, 0)</code>, they would see in "F3" that
     * they are standing at <code>(-128, -128)</code>.
     *
     * <p>This Offset is subject to change, for example if the Player changes worlds.</p>
     *
     * @param player A player currently logged in to the server. Use {@link #adaptPlayer(Object)} to convert
     *               a platform-specific instance (such as a Bukkit <code>Player</code>) to an {@link OffsetPlayer}, or
     *               {@link #getPlayer(UUID)} to get a player by their UUID.
     * @return The coordinate Offset this player sees, or <code>Offset.ZERO</code> if the player has no offset.
     */
    Offset getOffset(OffsetPlayer player);

    /**
     * Get an {@link OffsetPlayer} instance for a player currently connected to the server by their UUID.
     *
     * @param playerUuid The UUID of a player currently connected to the server.
     * @return An OffsetPlayer instance for the player, or null if no matching player is connected.
     */
    @Nullable OffsetPlayer getPlayer(UUID playerUuid);

    /**
     * Adapt a platform-specific player object (such as a Bukkit <code>Player</code>) into an {@link OffsetPlayer}.
     *
     * @param platformPlayerObject A platform-specific player object. The exact type depends on the platform adapter
     *                             in use. For example, on a Paper server, this would be an instance of
     *                             <code>org.bukkit.entity.Player</code>.
     * @return An OffsetPlayer instance for the player.
     * @throws ClassCastException if the provided object is not of the expected type for the running platform.
     */
    OffsetPlayer adaptPlayer(Object platformPlayerObject);

    /**
     * Get running configuration of the CoordinateOffset plugin.
     *
     * <p>Configured Offset Providers are not accessible here because they load after the main CoordinateOffset config.
     * See {@link CoordinateOffsetAPI#getProviderConfig()} for provider-specific configuration access.</p>
     *
     * @return The current configuration.
     */
    CoordinateOffsetConfig getConfig();

    /**
     * Get Offset Providers configured in the CoordinateOffset configuration.
     *
     * <p>Offset Providers load <b>after</b> external plugins have a chance to register new OffsetProvider classes,
     * so this function must only be called after the server has finished starting up.</p>
     *
     * <p>See {@link CoordinateOffsetAPI#getConfig()} for general configuration access.</p>
     *
     * @return The current configuration.
     *
     * @throws IllegalStateException if Offset Provider configuration has not yet been loaded.
     */
    CoordinateOffsetProviderConfig getProviderConfig();

    /**
     * Register a new OffsetProvider class that can be used in the CoordinateOffset configuration.
     *
     * @param className A class name to identify the provider in the configuration. Users may activate this provider
     *                  by creating a provider in the CoordinateOffset config.yml with a <code>class:</code> parameter
     *                  matching this name. This should be a simple string matching the class name of the provider,
     *                  for example <code>"MyOffsetProvider"</code>.
     * @param factory A factory to create instances of the provider from configuration data. For examples, see
     *                {@link OffsetProvider.ConfigurationFactory} and the built-in providers.
     */
    void registerOffsetProviderClass(
        String className,
        OffsetProvider.ConfigurationFactory<? extends OffsetProvider> factory
    );
}
