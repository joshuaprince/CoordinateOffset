package com.jtprince.coordinateoffset.command;

import com.jtprince.coordinateoffset.ScalableOffset;
import com.jtprince.coordinateoffset.adapter.OffsetPlayer;
import com.jtprince.coordinateoffset.provider.OffsetProvider;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * Container for data and response methods for "/offset set" commands.
 */
@NullMarked
public class OffsetSetCommand implements OffsetCommand {
    private final OffsetCommandSender commandSender;
    private final List<OffsetPlayer> targets;
    private final ScalableOffset offset;

    private @Nullable OffsetProvider notPersistentForProvider = null;
    private final Set<OffsetPlayer> notPersistentInProviderTargets = new HashSet<>();
    private final Map<OffsetPlayer, Double> scalingForTargets = new HashMap<>();

    public OffsetSetCommand(OffsetCommandSender commandSender, List<? extends OffsetPlayer> targets, ScalableOffset offset) {
        this.commandSender = commandSender;
        this.targets = List.copyOf(targets);
        this.offset = offset;
    }

    @Override
    public OffsetCommandSender getCommandSender() {
        return commandSender;
    }

    /**
     * Get an immutable list of the player(s) whose offset is being set by a command.
     */
    public List<OffsetPlayer> getTargets() {
        return targets;
    }

    /**
     * Get the offset that was specified in the command.
     */
    public ScalableOffset getOffset() {
        return offset;
    }

    /**
     * Print a warning message to the sender that the specified offset will be lost the next time an offset can change
     * for a given player.
     *
     * @param provider Offset provider that is not persisting the offset. The name is printed in the warning message.
     * @param target Player whose offset will be lost.
     */
    public void warnOffsetIsNotPersistentInProvider(OffsetProvider provider, OffsetPlayer target) {
        notPersistentForProvider = provider;
        notPersistentInProviderTargets.add(target);
    }

    /**
     * Check if the offset provider affected by this command has called {@link #warnOffsetIsNotPersistentInProvider}.
     *
     * @param target Player whose offset is being set.
     * @return The offset provider that indicated it is not storing the specified offset, or null if no offset provider
     *         indicated it is not storing the offset.
     */
    public @Nullable OffsetProvider getOffsetIsNotPersistentForProvider(OffsetPlayer target) {
        if (notPersistentInProviderTargets.contains(target)) {
            return notPersistentForProvider;
        } else {
            return null;
        }
    }

    /**
     * Print a warning message to the sender that the specified offset will be scaled by the specified factor since the
     * targeted player is in a world with a non-unit scaling factor.
     *
     * @param target Player whose offset will be scaled.
     * @param scalingFactor The scaling factor (divisor) that will be applied to the offset.
     */
    public void warnScaling(OffsetPlayer target, double scalingFactor) {
        scalingForTargets.put(target, scalingFactor);
    }

    /**
     * Retrieve the scaling factor that will be applied to the offset for the specified player.
     *
     * @param target Player whose offset scaling factor is being retrieved.
     * @return The scaling factor (divisor) that will be applied to the offset, or null if no scaling warning is set.
     */
    public @Nullable Double getWarnScaling(OffsetPlayer target) {
        return scalingForTargets.get(target);
    }
}
