package com.jtprince.coordinateoffset.provider;

import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.adapter.OffsetPlayer;
import com.jtprince.coordinateoffset.api.CoordinateOffsetAPI;
import de.exlll.configlib.Configuration;
import de.exlll.configlib.Polymorphic;
import org.jspecify.annotations.NullMarked;

import java.util.SequencedMap;
import java.util.UUID;

/**
 * An OffsetProvider is responsible for generating {@link Offset Offsets} for players in worlds.
 *
 * <p>External API consumers may register their own OffsetProvider classes (see
 * {@link CoordinateOffsetAPI#registerOffsetProviderClass}). Users may then configure the registered provider class
 * in an OffsetProvider in the CoordinateOffset config.yml. A registered provider class does nothing until a user
 * creates an OffsetProvider in config.yml AND applies the provider.</p>
 *
 * <p>OffsetProviders may maintain state about players, either transiently (in memory) or persistently (on disk). If
 * persistent state is maintained, it is the responsibility of the OffsetProvider to save that state when appropriate,
 * and to load that state when appropriate.
 */
@Polymorphic
@Configuration
@NullMarked
public abstract class OffsetProvider {
    public static int OFFSET_MAX = 30_000_000;

    public final String name;

    public OffsetProvider(String userDefinedProviderName) {
        this.name = userDefinedProviderName;
    }

    /**
     * Generate a coordinate {@link Offset} for a specific player in a world.
     *
     * <p>This function is called whenever the player's Offset has an opportunity to change. The reasons that an Offset
     * might be changing are enumerated in {@link OffsetProviderContext.ProvideReason}.</p>
     *
     * @param context Container for all context associated with this Offset change, such as the {@link OffsetPlayer}
     *                this Offset will be for, and the name of the World the Offset will be applied in.
     * @return The desired offset for this player.
     */
    public abstract Offset provideOffset(OffsetProviderContext context);

    /**
     * Called on this provider whenever a player leaves. The provider may override this function to write to disk
     * any persistent saved state related to that player.
     *
     * <p>This is usually called <b>before</b> {@link OffsetProvider#onPlayerDisconnect(UUID)}.</p>
     *
     * <p>The implementor should NOT assume that {@link #provideOffset} has been called with this Player at any point
     * before this.</p>
     *
     * @see OffsetProvider#onPlayerDisconnect(UUID)
     * @param player The player that's leaving.
     */
    public void onPlayerQuit(OffsetPlayer player) {}

    /**
     * Called on this provider when a player's connection is closed. The provider may override this function to clean
     * up any cached and/or transient state related to that player.
     *
     * <p>This is usually called <b>after</b> {@link OffsetProvider#onPlayerQuit(OffsetPlayer)} and may be called after
     * the player has already left the server.</p>
     *
     * <p>The implementor should NOT assume that {@link #provideOffset} has been called with this Player at any point
     * before this.</p>
     *
     * @see OffsetProvider#onPlayerQuit(OffsetPlayer)
     * @param playerUuid The UUID of the player that has disconnected. This is NOT guaranteed to correspond to an
     *                   online Player at the time this function is called.
     */
    public void onPlayerDisconnect(UUID playerUuid) {}

    public abstract SequencedMap<String, ?> serialize();
}
