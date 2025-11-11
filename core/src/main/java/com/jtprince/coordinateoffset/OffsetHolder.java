package com.jtprince.coordinateoffset;

import com.jtprince.coordinateoffset.adapter.OffsetLocation;
import com.jtprince.coordinateoffset.adapter.OffsetPlayer;
import com.jtprince.coordinateoffset.command.OffsetSetCommand;
import com.jtprince.coordinateoffset.provider.OffsetProvider;
import com.jtprince.coordinateoffset.provider.OffsetProviderContext;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

@NullMarked
public class OffsetHolder {
    private final CoordinateOffsetCore core;
    private final OffsetFactory offsetFactory;
    OffsetHolder(CoordinateOffsetCore core) {
        this.core = core;
        this.offsetFactory = new OffsetFactory(core);
    }

    /**
     * Immutable container for player offset data.
     *
     * @param previousOffset Offset the player had before the most recent offset was applied. This may be the same as
     *                       the current offset.
     * @param currentOffset Offset the player has now and most packets will use.
     * @param nextOffset Offset that the player will have as soon as the server sends the next "position" packet.
     *                   Offset creation logic on the main thread writes to this field, then the Netty thread swaps it
     *                   into current.
     */
    private record PlayerOffsetData(
        OffsetData previousOffset,
        OffsetData currentOffset,
        @Nullable OffsetData nextOffset
    ) {}
    private final ConcurrentHashMap<UUID, PlayerOffsetData> playerOffsetData = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Object> pendingDataLocks = new ConcurrentHashMap<>();

    /**
     * Get the current offset applied to a Player.
     *
     * <p>If the player's offset is about to change or changing, this will reflect the *previous* offset until the
     * server sends a "position" packet.</p>
     *
     * This method is safe to call on any thread.
     *
     * @param player Player to query.
     * @return The player's current offset in the world they are in.
     * @throws NoSuchElementException If the player has no offset data or has not yet received a POSITION packet.
     */
    public OffsetData getOffset(OffsetPlayer player) {
        PlayerOffsetData data = playerOffsetData.get(player.getUuid());
        if (data == null) {
            throw new NoSuchElementException("Player " + player.getName() + " has no offset data!");
        }
        return data.currentOffset;
    }

    /**
     * Look ahead at the next generated offset, which a Player will have after a "position" packet is sent.
     *
     * <p>This is useful for packets like RESPAWN, which refer to coordinates in the world a player is going to before
     * sending a "position" packet to move the player to that world.</p>
     *
     * <p>This should only be called on a Netty thread, but is safe to call on any thread.</p>
     *
     * @param player Player to query.
     * @return The player's next offset in the world they will soon be in, or the current offset if the player has no
     *         next offset.
     */
    public OffsetData getNextOffset(OffsetPlayer player) {
        PlayerOffsetData data = playerOffsetData.get(player.getUuid());
        if (data == null) {
            throw new NoSuchElementException("Player " + player.getName() + " has no offset data!");
        }
        return (data.nextOffset == null) ? data.currentOffset : data.nextOffset;
    }

    /**
     * Block the current thread until the player's offset has been generated, then get that offset.
     *
     * <p>This is useful for JOIN_GAME packets. These packets contain coordinates which must be offset, but the packets
     * are sent concurrently with offset generation occurring on the main thread. See OffsetChangeSequencePaper.md
     * in the project's <code>docs</code> directory for more information.</p>
     *
     * <p>This must only be called on a Netty thread. Blocking the main thread is not acceptable.</p>
     *
     * @param playerUuid Player to query.
     * @param timeoutMillis Maximum time to block the thread if the player's offset has not yet been generated.
     * @return The player's offset in the world they will soon be in.
     * @throws TimeoutException If the player's offset has not yet been generated and the timeout has been reached.
     */
    public Offset waitForJoiningOffset(UUID playerUuid, int timeoutMillis) throws TimeoutException {
        PlayerOffsetData data = playerOffsetData.get(playerUuid);
        if (data == null && timeoutMillis > 0) {
            /*
             * Concurrency hack:
             * Paper *concurrently* (a) calls PlayerJoinEvent and (b) sends a JOIN_GAME packet.
             * The JOIN_GAME packet needs to be offsetted. But the offset isn't generated until PlayerJoinEvent
             *   (PlayerSpawnLocationEvent is deprecated since 1.21.9).
             * This hack is to block the Netty thread until the joining player gets an offset.
             */
            Object pendingOffsetDataLock = pendingDataLocks.computeIfAbsent(playerUuid, uuid -> new Object());
            long now = System.currentTimeMillis();
            long deadline = now + timeoutMillis;
            synchronized (pendingOffsetDataLock) {
                while (data == null && (now = System.currentTimeMillis()) < deadline) {
                    try {
                        pendingOffsetDataLock.wait( deadline - now);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    data = playerOffsetData.get(playerUuid);
                }
            }
        }
        if (data == null) {
            throw new TimeoutException("Player " + playerUuid + " has no offset data!");
        }
        return data.currentOffset.offset();
    }

    /**
     * Generate or regenerate the offset a player will have next based on the context the player will be in.
     * Store that offset in this holder.
     *
     * <p>This must only be called on the main server thread.</p>
     *
     * <p>Note that this does not immediately change the player's offset. Offset changes themselves happen in response
     * to certain "position" packets. However, generating a <code>nextOffset</code> will set up an offset change for
     * when the "position" packet occurs.</p>
     *
     * @param player Player to generate offset for.
     * @param previousLocation Previous location of the player, or null if the player is joining the server.
     * @param nextLocation Location the player is about to be.
     * @param reason Reason for generating a new offset.
     * @return Result containing the new offset and whether the offset changed.
     */
    public OffsetChange generateNextOffset(
        OffsetPlayer player,
        @Nullable OffsetLocation previousLocation,
        OffsetLocation nextLocation,
        OffsetProviderContext.ProvideReason reason
    ) {
        PlayerOffsetData data = playerOffsetData.get(player.getUuid());
        OffsetProviderContext context = new OffsetProviderContext(
            player, previousLocation, nextLocation, data == null ? null : data.currentOffset.offset(), reason);
        OffsetData creation = offsetFactory.createOffset(context);
        return setNextOffset(context.player().getUuid(), creation);
    }

    /**
     * Set a player's next offset to a specific offset based on an incoming command.
     *
     * <p>This must only be called on the main server thread.</p>
     *
     * <p>Note that this does not immediately change the player's offset. Offset changes themselves happen in response
     * to certain "position" packets. However, generating a <code>nextOffset</code> will set up an offset change for
     * when the "position" packet occurs.</p>
     *
     * @param player Player to set offset for.
     * @param playerLocation Location the player currently is. Assumed real location is not changing upon a command.
     * @param setCommand Command that triggered the offset change, containing the new offset.
     * @return Result containing the new offset and whether the offset changed.
     */
    public OffsetChange setNextOffsetByCommand(
        OffsetPlayer player,
        OffsetLocation playerLocation,
        OffsetSetCommand setCommand
    ) {
        PlayerOffsetData playerCache = playerOffsetData.get(player.getUuid());
        OffsetData currentOffsetData = (playerCache == null ? null : playerCache.currentOffset);
        Offset currentOffset = (currentOffsetData == null ? null : currentOffsetData.offset());

        // The player's context is whatever their current status is when the command is run.
        // The previous and current locations are the same since the player isn't teleporting.
        OffsetProviderContext context = new OffsetProviderContext(
            player, playerLocation, playerLocation, currentOffset,
            OffsetProviderContext.ProvideReason.COMMAND_SET);

        // Inform current provider that the offset is being set by a command
        OffsetProvider affectedProvider = getAffectedProvider(currentOffsetData);
        if (affectedProvider != null) {
            try {
                affectedProvider.onOffsetSetByCommand(setCommand, player);
            } catch (Exception e) {
                new RuntimeException("Error informing affected offset provider " + affectedProvider.name +
                    " of offset set by command.", e).printStackTrace();
            }
        }

        OffsetData creation = offsetFactory.createSpecificOffset(
            setCommand.getOffset(), new OffsetData.Source.SetCommand(setCommand, affectedProvider), context);
        return setNextOffset(player.getUuid(), creation);
    }

    private OffsetChange setNextOffset(UUID playerUuid, OffsetData newOffset) {
        PlayerOffsetData d = playerOffsetData.compute(playerUuid, (uuid, existingOffsetData) -> {
            if (existingOffsetData == null) {
                debugLog("Generate first: " + newOffset + ", " + newOffset + ", " + null);
                log(newOffset);
                return new PlayerOffsetData(
                    newOffset,
                    newOffset,
                    null
                );
            }

            if (existingOffsetData.currentOffset.offset().equals(newOffset.offset())) {
                /*
                 * Shortcut: If the player's actual offset components haven't changed, don't bother setting next.
                 * Just immediately swap in the new offset.
                 * This can happen if the offset source changed compared to what we have in current now.
                 */
                debugLog("Unchanged offset:" + existingOffsetData.previousOffset + " , " + newOffset + ", " + existingOffsetData.nextOffset);
                return new PlayerOffsetData(
                    existingOffsetData.previousOffset, // Keep previous the same
                    newOffset,  // Immediately swap in to current
                    existingOffsetData.nextOffset // Keep next the same
                );
            }

            debugLog("Generate next: " + existingOffsetData.previousOffset + ", " + existingOffsetData.currentOffset + ", " + newOffset);
            return new PlayerOffsetData(
                existingOffsetData.previousOffset, // Keep previous the same
                existingOffsetData.currentOffset,  // Keep current the same
                newOffset // Set next
            );
        });

        Object pendingOffsetDataLock = pendingDataLocks.computeIfAbsent(playerUuid, uuid -> new Object());
        synchronized (pendingOffsetDataLock) {
            pendingOffsetDataLock.notifyAll();
        }

        OffsetChange offsetChange = new OffsetChange(
            d.currentOffset,
            (d.nextOffset == null ? d.currentOffset : d.nextOffset)
        );
        if (offsetChange.offsetChanged()) {
            log(offsetChange.newOffsetData());
        }

        return offsetChange;
    }

    /**
     * Shift the player's <code>nextOffset</code> into <code>currentOffset</code>, and <code>currentOffset</code> into
     * <code>previousOffset</code>. Following calls to {@link #getOffset(OffsetPlayer)} will return the new current
     * offset.
     *
     * <p>This should only be called on a Netty thread, but is safe to call on any thread.</p>
     *
     * @param player Player to update.
     */
    public void swapInNextOffset(OffsetPlayer player) {
        playerOffsetData.computeIfPresent(player.getUuid(), (uuid, existingOffsetData) -> {
            if (existingOffsetData.nextOffset == null) {
                // No next offset, so don't swap in anything.
                return existingOffsetData;
            }
            debugLog("Swap in next: " +
                existingOffsetData.currentOffset + ", " +
                existingOffsetData.nextOffset + ", null");
            return new PlayerOffsetData(
                existingOffsetData.currentOffset, // Swap current into previous
                existingOffsetData.nextOffset, // Swap next into current
                null // Clear next
            );
        });
    }

    /**
     * Drop all data about a player from this holder. This should be called when a player disconnects from the server.
     *
     * <p>This method is safe to call on any thread.</p>
     *
     * @param uuid The UUID of the player to drop, presumably who is disconnecting from the server.
     */
    public void remove(UUID uuid) {
        playerOffsetData.remove(uuid);
        pendingDataLocks.remove(uuid);
    }

    private void log(OffsetData offset) {
        if (!core.getConfig().getVerbose()) return;

        StringBuilder s = new StringBuilder();
        s.append("Using ");
        s.append(offset.offset());
        s.append(" from ");
        switch (offset.source()) {
            case OffsetData.Source.PermissionBypass ignored -> s.append("permission bypass");
            case OffsetData.Source.BedrockBypass ignored -> { return; /* Warning logged in OffsetCreator on Join only */ }
            case OffsetData.Source.Provider p -> {
                s.append("provider \"").append(p.provider().name).append("\"");
                if (p.isOverride()) {
                    s.append(" (config.yml override)");
                } else {
                    s.append(" (default provider)");
                }
            }
            case OffsetData.Source.SetCommand p -> s.append("command by ").append(p.command().getCommandSender().name());
        }
        s.append(" for player ");
        s.append(offset.context().player().getName());
        s.append(" in world \"");
        s.append(offset.context().playerLocation().getWorld().getName());
        s.append("\"");
        s.append(switch (offset.context().reason()) {
            case JOIN -> " (player joined)";
            case DEATH_RESPAWN -> " (player respawned)";
            case WORLD_CHANGE -> " (player changed worlds)";
            case TELEPORT -> " (player teleported)";
            case COMMAND_REGENERATE -> " (regenerated by command)";
            case COMMAND_SET -> ""; // already mentioned by source
            case PLUGIN_REGENERATE -> " (regenerated by external plugin)";
        });

        core.getLogger().info(s.toString());
    }

    private void debugLog(String message) {
        if (core.isDebugEnabled()) {
            core.getLogger().info("[Debug] " + message);
        }
    }

    /**
     * Determine which offset provider should be informed of the offset change initiated by a command.
     */
    private @Nullable OffsetProvider getAffectedProvider(@Nullable OffsetData currentOffset) {
        if (currentOffset == null) return null;
        OffsetProvider affectedProvider = switch (currentOffset.source()) {
            case OffsetData.Source.BedrockBypass ignored -> null;
            case OffsetData.Source.PermissionBypass ignored -> null;
            case OffsetData.Source.Provider provider -> provider.provider();
            case OffsetData.Source.SetCommand setCommand -> setCommand.affectedProvider();
        };
        if (affectedProvider == null) return null;

        // In case config was reloaded and the provider object changed, get the new provider object to inform
        OffsetProvider reloadedProvider =
            core.getProviderConfig().getAllOffsetProviderConfigs().get(affectedProvider.name);
        if (reloadedProvider != null) {
            return reloadedProvider;
        }

        return affectedProvider;
    }
}
