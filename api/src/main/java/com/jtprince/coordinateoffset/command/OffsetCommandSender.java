package com.jtprince.coordinateoffset.command;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import org.jspecify.annotations.NullMarked;

import java.util.Collections;

@NullMarked
public record OffsetCommandSender(Audience audience, String name) implements ForwardingAudience {
    @Override
    public Iterable<? extends Audience> audiences() {
        return Collections.singleton(audience);
    }
}
