package com.jtprince.coordinateoffset.command;

import com.jtprince.coordinateoffset.adapter.OffsetPlayer;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class OffsetQueryCommand implements OffsetCommand {
    private final OffsetCommandSender commandSender;
    private final OffsetPlayer target;
    private final boolean verbose;

    public OffsetQueryCommand(OffsetCommandSender commandSender, OffsetPlayer target, boolean verbose) {
        this.commandSender = commandSender;
        this.target = target;
        this.verbose = verbose;
    }

    @Override
    public OffsetCommandSender getCommandSender() {
        return commandSender;
    }

    public OffsetPlayer getTarget() {
        return target;
    }

    public boolean isVerbose() {
        return verbose;
    }
}
