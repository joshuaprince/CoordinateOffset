package com.jtprince.coordinateoffset.command;

import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.adapter.OffsetPlayer;
import com.jtprince.coordinateoffset.provider.OffsetProvider;
import org.jspecify.annotations.NullMarked;

import java.util.List;

/**
 * Container for data and methods to respond to "/offset set" commands.
 */
@NullMarked
public interface OffsetSetCommand extends OffsetCommand {
    /**
     * Get an immutable list of the player(s) whose offset is being set by a command.
     */
    List<OffsetPlayer> getTargets();

    /**
     * Get the offset that was specified in the command.
     */
    Offset getOffset();

    /**
     * Print a warning message to the sender that the specified offset will be lost the next time an offset can change.
     *
     * @param provider Offset provider that is not persisting the offset. The name is printed in the warning message.
     * @param target Player whose offset will be lost.
     */
    void warnOffsetNotPersistentInProvider(OffsetProvider provider, OffsetPlayer target);
}
