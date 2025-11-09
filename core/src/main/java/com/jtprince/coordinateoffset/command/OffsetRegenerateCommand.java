package com.jtprince.coordinateoffset.command;

import com.jtprince.coordinateoffset.adapter.OffsetPlayer;
import org.jspecify.annotations.NullMarked;

import java.util.List;

@NullMarked
public class OffsetRegenerateCommand implements OffsetCommand {
    private final OffsetCommandSender commandSender;
    private final List<OffsetPlayer> targets;

    public OffsetRegenerateCommand(OffsetCommandSender commandSender, List<? extends OffsetPlayer> targets) {
        this.commandSender = commandSender;
        this.targets = List.copyOf(targets);
    }

    @Override
    public OffsetCommandSender getCommandSender() {
        return commandSender;
    }

    public List<OffsetPlayer> getTargets() {
        return targets;
    }
}
