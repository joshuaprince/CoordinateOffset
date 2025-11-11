package com.jtprince.coordinateoffset.api;

import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.OffsetChange;
import com.jtprince.coordinateoffset.OffsetData;
import com.jtprince.coordinateoffset.adapter.OffsetLocation;
import com.jtprince.coordinateoffset.adapter.OffsetPlayer;
import com.jtprince.coordinateoffset.config.CoordinateOffsetConfig;
import com.jtprince.coordinateoffset.config.CoordinateOffsetProviderConfig;
import com.jtprince.coordinateoffset.provider.OffsetProvider;
import com.jtprince.coordinateoffset.provider.OffsetProviderConfig;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.UUID;
import java.util.function.Function;

/**
 * API for the CoordinateOffset plugin.
 *
 * <p>External plugins may get a singleton instance of this API via {@link CoordinateOffset#api()}.</p>
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
     * <p>This method is safe to call from any thread.</p>
     *
     * @param player A player currently logged in to the server. Use {@link #adaptPlayer(Object)} to convert
     *               a platform-specific instance (such as a Bukkit <code>Player</code>) to an {@link OffsetPlayer}, or
     *               {@link #getPlayer(UUID)} to get a player by their UUID.
     * @return The coordinate offset this player sees, or <code>Offset.ZERO</code> if the player has no offset.
     * @see CoordinateOffsetAPI#getOffsetData
     */
    Offset getOffset(OffsetPlayer player);

    /**
     * Get information about a player's current offset, including the source from where the offset came and the
     * context from which the offset was generated.
     *
     * <p>This method is safe to call from any thread.</p>
     *
     * @param player A player currently logged in to the server. Use {@link #adaptPlayer(Object)} to convert
     *               a platform-specific instance (such as a Bukkit <code>Player</code>) to an {@link OffsetPlayer}, or
     *               {@link #getPlayer(UUID)} to get a player by their UUID.
     * @return A container for all known data about the player's current offset.
     */
    OffsetData getOffsetData(OffsetPlayer player);

    /**
     * Immediately regenerate the player's current offset by selecting a new offset from the applicably configured
     * offset provider. This forces a teleport effect on the player if their offset is changed, but it does not move
     * them in real coordinate space.
     *
     * <p>This method must <b>only</b> be called on the main server thread.</p>
     *
     * @param player A player currently logged in to the server. Use {@link #adaptPlayer(Object)} to convert
     *               a platform-specific instance (such as a Bukkit <code>Player</code>) to an {@link OffsetPlayer}, or
     *               {@link #getPlayer(UUID)} to get a player by their UUID.
     * @return A container with the player's previous and new offset, which can be inspected to determine if the offset
     *         was changed.
     */
    OffsetChange regenerateOffset(OffsetPlayer player);

    /**
     * Immediately set the player's current offset. This forces a teleport effect on the player if their offset is
     * changed, but it does not move them in real coordinate space.
     *
     * <p>Use of this function is <b>discouraged</b> because any applied offset will be lost as soon as the player's
     * offset has a chance to change. This means <b>any</b> teleport, world change, or relog will undo the offset
     * applied here. This is the case even if <code>regenerateOn*</code> options in the configuration are set to
     * false.</p>
     * 
     * <p>To apply an offset that persists, instead register an {@link OffsetProvider} with
     * {@link CoordinateOffsetAPI#registerOffsetProviderClass}, apply the offset provider in the plugin's configuration,
     * and call {@link #regenerateOffset}. Return the desired offset in your offset provider, and that offset will
     * continue to be applied every time the player's offset might change.</p>
     *
     * <p>This method must <b>only</b> be called on the main server thread.</p>
     *
     * @param player A player currently logged in to the server. Use {@link #adaptPlayer(Object)} to convert
     *               a platform-specific instance (such as a Bukkit <code>Player</code>) to an {@link OffsetPlayer}, or
     *               {@link #getPlayer(UUID)} to get a player by their UUID.
     * @param offset The new offset to apply.
     * @return A container with the player's previous and new offset, which can be inspected to determine if the offset
     *         was changed.
     */
    OffsetChange setOffset(OffsetPlayer player, Offset offset);

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
     * <p>This method is safe to call from any thread.</p>
     *
     * @param platformPlayerObject A platform-specific player object. The exact type depends on the platform adapter
     *                             in use. For example, on a Paper server, this would be an instance of
     *                             <code>org.bukkit.entity.Player</code>.
     * @return An OffsetPlayer instance for the player.
     * @throws ClassCastException if the provided object is not of the expected type for the running platform.
     */
    OffsetPlayer adaptPlayer(Object platformPlayerObject) throws ClassCastException;

    /**
     * Adapt a platform-specific location object (such as a Bukkit <code>Location</code>) into an
     * {@link OffsetLocation}.
     *
     * <p>This method is safe to call from any thread.</p>
     *
     * @param platformLocationObject A platform-specific location object. The exact type depends on the platform adapter
     *                               in use. For example, on a Paper server, this would be an instance of
     *                               <code>org.bukkit.Location</code>.
     * @return An OffsetLocation instance for the location.
     * @throws ClassCastException if the provided object is not of the expected type for the running platform.
     */
    OffsetLocation adaptLocation(Object platformLocationObject) throws ClassCastException;

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
     * @param deserializeFunction A function that can create instances of the provider from configuration data. For
     *                            examples, see the built-in providers.
     */
    void registerOffsetProviderClass(
        String className,
        Function<OffsetProviderConfig, OffsetProvider> deserializeFunction
    );
}
