package com.jtprince.coordinateoffset.command;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface OffsetCommand {
    /**
     * Get who sent this command. Can be used to respond to the command.
     */
    OffsetCommandSender getCommandSender();
}
