package com.jtprince.coordinateoffset.command;

import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.adapter.OffsetPlayer;
import com.jtprince.coordinateoffset.provider.OffsetProvider;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@NullMarked
public class OffsetSetCommandImpl implements OffsetSetCommand {
    private final OffsetCommandSender commandSender;
    private final List<OffsetPlayer> targets;
    private final Offset offset;

    private @Nullable OffsetProvider notPersistentForProvider = null;
    private final Set<OffsetPlayer> notPersistentInProviderTargets = new HashSet<>();

    public OffsetSetCommandImpl(OffsetCommandSender commandSender, List<? extends OffsetPlayer> targets, Offset offset) {
        this.commandSender = commandSender;
        this.targets = List.copyOf(targets);
        this.offset = offset;
    }

    @Override
    public OffsetCommandSender getCommandSender() {
        return commandSender;
    }

    @Override
    public List<OffsetPlayer> getTargets() {
        return targets;
    }

    @Override
    public Offset getOffset() {
        return offset;
    }

    @Override
    public void warnOffsetNotPersistentInProvider(OffsetProvider provider, OffsetPlayer target) {
        notPersistentForProvider = provider;
        notPersistentInProviderTargets.add(target);
    }

    public @Nullable OffsetProvider getNotPersistentForProvider(OffsetPlayer target) {
        if (notPersistentInProviderTargets.contains(target)) {
            return notPersistentForProvider;
        } else {
            return null;
        }
    }
}
