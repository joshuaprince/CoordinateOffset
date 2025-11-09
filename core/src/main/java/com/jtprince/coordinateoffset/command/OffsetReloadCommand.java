package com.jtprince.coordinateoffset.command;

import org.jspecify.annotations.NullMarked;

@NullMarked
public class OffsetReloadCommand implements OffsetCommand {
    private final OffsetCommandSender commandSender;

    public OffsetReloadCommand(OffsetCommandSender commandSender) {
        this.commandSender = commandSender;
    }

    @Override
    public OffsetCommandSender getCommandSender() {
        return commandSender;
    }
}
