package com.jtprince.coordinateoffset.command;

import com.jtprince.coordinateoffset.CoordinateOffsetCore;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.OffsetChange;
import com.jtprince.coordinateoffset.OffsetFactory;
import com.jtprince.coordinateoffset.adapter.OffsetLocation;
import com.jtprince.coordinateoffset.adapter.OffsetPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OffsetCommandExecutor {
    private final CoordinateOffsetCore core;
    public OffsetCommandExecutor(CoordinateOffsetCore core) {
        this.core = core;
    }

    public enum Result {
        SUCCESS,
        FAIL
    }

    public Result execute(OffsetSetCommand command) {
        List<OffsetPlayer> successfulTargets = new ArrayList<>();
        for (OffsetPlayer target : command.getTargets()) {
            OffsetChange result = core.getOffsetHolder().setNextOffsetByCommand(target, target.getLocation(), command);
            if (!result.offsetChanged()) {
                command.getCommandSender().sendMessage(formatUnchangedOffsetMessage(target, result.getCommandSenderResponse()));
                continue;
            }

            core.getAdapter().getOffsetSwapper().forceOffsetSwap(target);

            if (OffsetFactory.canBypassByPermission(target)) {
                command.getCommandSender().sendMessage(Component.empty()
                    .color(NamedTextColor.GRAY)
                    .decorate(TextDecoration.ITALIC)
                    .append(Component.text("  Warning: offset for "))
                    .append(formatPlayerName(target))
                    .append(Component.text(" is not persistent (player has offset bypass permission)."))
                );
            } else if (command.getOffsetIsNotPersistentForProvider(target) != null) {
                command.getCommandSender().sendMessage(Component.empty()
                    .color(NamedTextColor.GRAY)
                    .decorate(TextDecoration.ITALIC)
                    .append(Component.text("  Warning: offset for "))
                    .append(formatPlayerName(target))
                    .append(Component.text(" is not persistent (offset provider does not store offsets)."))
                );
            }

            successfulTargets.add(target);
        }

        if (!successfulTargets.isEmpty()) {
            command.getCommandSender().sendMessage(Component.text("Set coordinate offset for ")
                .color(NamedTextColor.GRAY)
                .append(formatPlayerNames(successfulTargets))
                .append(Component.text(" to "))
                .append(formatOffset(command.getOffset()))
                .append(Component.text("."))
            );
        }

        return successfulTargets.isEmpty() ? Result.FAIL : Result.SUCCESS;
    }

    private Component formatOffset(Offset offset) {
        return Component.text("[x=")
            .append(Component.text(offset.x()).color(NamedTextColor.YELLOW))
            .append(Component.text(", z="))
            .append(Component.text(offset.z()).color(NamedTextColor.YELLOW))
            .append(Component.text("]"))
            .color(NamedTextColor.DARK_AQUA);
    }

    private Component formatLocation(OffsetLocation location) {
        return Component.text("[x=")
            .append(Component.text((int) location.getX()).color(NamedTextColor.GOLD))
            .append(Component.text(", y="))
            .append(Component.text((int) location.getY()).color(NamedTextColor.GOLD))
            .append(Component.text(", z="))
            .append(Component.text((int) location.getZ()).color(NamedTextColor.GOLD))
            .append(Component.text("]"))
            .color(NamedTextColor.LIGHT_PURPLE);
    }

    private static final TextColor PLAYER_NAME_COLOR = TextColor.color(0x18a9ff);

    private Component formatPlayerName(OffsetPlayer player) {
        return Component.text(player.getName())
            .color(PLAYER_NAME_COLOR)
            .hoverEvent(Component.text(player.getUuid().toString()));
    }

    private Component formatPlayerNames(List<? extends OffsetPlayer> players) {
        if (players.size() == 1) {
            return formatPlayerName(players.getFirst());
        } else {
            return Component.text(players.size() + " players")
                .color(TextColor.color(0x18a9ff))
                .hoverEvent(Component.text(players.stream()
                    .map(OffsetPlayer::getName)
                    .collect(Collectors.joining(", "))));
        }
    }

    private Component formatUnchangedOffsetMessage(OffsetPlayer target, @Nullable String message) {
        return Component.empty()
            .color(message == null ? NamedTextColor.YELLOW : NamedTextColor.RED)
            .decorate(TextDecoration.ITALIC)
            .append(Component.text("Coordinate offset unchanged for "))
            .append(formatPlayerName(target))
            .append(message != null ? (Component.text(": " + message)) : Component.text("."));
    }
}
