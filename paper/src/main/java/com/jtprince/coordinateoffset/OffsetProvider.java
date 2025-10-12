package com.jtprince.coordinateoffset;

import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Offset Providers are an extensible way to determine what {@link Offset} each player should have in each world.
 */
public abstract class OffsetProvider {
    public static int OFFSET_MAX = 30_000_000;

    public final @NotNull String name;

    public OffsetProvider(@NotNull String name) {
        this.name = name;
    }

    /**
     * Generate a coordinate {@link Offset} for a specific player in a world.
     *
     * <p>This function is called whenever the player's Offset has an opportunity to change. The reasons that an Offset
     * might be changing are enumerated in {@link OffsetProviderContext.ProvideReason}.</p>
     *
     * @param context Container for all context associated with this Offset change, such as the {@link Player} this
     *                Offset will be for, and the {@link World} the Offset will be applied to.
     * @return The desired offset for this player.
     */
    public abstract @NotNull Offset getOffset(@NotNull OffsetProviderContext context);

    /**
     * Called on this provider whenever a player leaves. The provider may override this function to write to disk
     * any persistent saved state related to that player.
     *
     * <p>This is usually called <b>before</b> {@link OffsetProvider#onPlayerDisconnect(UUID)}.</p>
     *
     * <p>The implementor should NOT assume that {@link #getOffset} has been called with this Player at any point before
     * this.</p>
     *
     * @see OffsetProvider#onPlayerDisconnect(UUID)
     * @param player The player that's leaving.
     */
    public void onPlayerQuit(@NotNull Player player) {}

    /**
     * Called on this provider when a player's connection is closed. The provider may override this function to clean
     * up any cached and/or transient state related to that player.
     *
     * <p>This is usually called <b>after</b> {@link OffsetProvider#onPlayerQuit(Player)} and may be called after
     * the player has already left the server.</p>
     *
     * <p>The implementor should NOT assume that {@link #getOffset} has been called with this Player at any point before
     * this.</p>
     *
     * @see OffsetProvider#onPlayerQuit(Player)
     * @param playerUuid The UUID of the player that has disconnected. This is NOT guaranteed to correspond to an online Player
     *                   at the time this function is called.
     */
    public void onPlayerDisconnect(@NotNull UUID playerUuid) {}

    /**
     * An {@link ConfigurationFactory} encodes how the CoordinateOffset plugin should create providers defined by the
     * user in <code>config.yml</code>.
     * @param <T> The type of OffsetProvider that this factory will create.
     */
    public interface ConfigurationFactory<T extends OffsetProvider> {
        @NotNull T createProvider(
                @NotNull String name, @NotNull CoordinateOffset plugin, @NotNull ConfigurationSection providerConfig
        ) throws IllegalArgumentException;
    }
}
