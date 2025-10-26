package com.jtprince.coordinateoffset.paper;

import com.jtprince.coordinateoffset.CoordinateOffsetCore;
import com.jtprince.coordinateoffset.CoordinateOffsetPermission;
import com.jtprince.coordinateoffset.CreatedOffset;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.adapter.OffsetPlayer;
import com.jtprince.coordinateoffset.paper.adapter.PaperLocation;
import com.jtprince.coordinateoffset.paper.adapter.PaperOffsetPlayer;
import com.jtprince.coordinateoffset.provider.OffsetProviderContext;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class OffsetCommand {
    /** Players with any of these permissions can see and the root /offset command. */
    private static final Set<CoordinateOffsetPermission> ROOT_COMMAND_PERMS = Set.of(
        CoordinateOffsetPermission.QUERY_SELF,
        CoordinateOffsetPermission.QUERY_OTHERS,
        CoordinateOffsetPermission.RELOAD,
        CoordinateOffsetPermission.REGENERATE_SELF,
        CoordinateOffsetPermission.REGENERATE_OTHERS,
        CoordinateOffsetPermission.SET_SELF,
        CoordinateOffsetPermission.SET_OTHERS
    );

    private final CoordinateOffsetPaperPlugin plugin;

    public OffsetCommand(CoordinateOffsetPaperPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerCommands() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("offset");

        // /offset - shortcut for /offset query
        // Permissions for /offset are checked in the executor for plain /offset
        root
            .requires(sender -> pluginEnabled() &&
                ROOT_COMMAND_PERMS.stream().anyMatch(p -> sender.getSender().hasPermission(p.node)))
            .executes(this::querySelf);

        // /offset query [<player>]
        root.then(Commands.literal("query")
            .requires(sender -> pluginEnabled() &&
                (sender.getSender().hasPermission(CoordinateOffsetPermission.QUERY_SELF.node) ||
                    sender.getSender().hasPermission(CoordinateOffsetPermission.QUERY_OTHERS.node)))
            .executes(this::querySelf)
                .then(Commands.argument("player", ArgumentTypes.player())
                    .requires(sender -> pluginEnabled() &&
                        sender.getSender().hasPermission(CoordinateOffsetPermission.QUERY_OTHERS.node))
                    .executes(this::queryOther)));

        // /offset reload
        root.then(Commands.literal("reload")
            .requires(sender -> pluginEnabled() &&
                sender.getSender().hasPermission(CoordinateOffsetPermission.RELOAD.node))
            .executes(this::reload));

        // /offset regenerate [<player>]
        root.then(Commands.literal("regenerate")
            .requires(sender -> pluginEnabled() &&
                (sender.getSender().hasPermission(CoordinateOffsetPermission.REGENERATE_SELF.node) ||
                sender.getSender().hasPermission(CoordinateOffsetPermission.REGENERATE_OTHERS.node)))
            .executes(this::regenerate)
                .then(Commands.argument("players", ArgumentTypes.players())
                    .requires(sender -> pluginEnabled() &&
                        sender.getSender().hasPermission(CoordinateOffsetPermission.REGENERATE_OTHERS.node))
                    .executes(this::regenerate)));

        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(root.build());
        });

        // /offset set x z [<player>]
        root.then(Commands.literal("set")
            .requires(sender -> pluginEnabled() &&
                (sender.getSender().hasPermission(CoordinateOffsetPermission.SET_SELF.node) ||
                sender.getSender().hasPermission(CoordinateOffsetPermission.SET_OTHERS.node)))
            .then(Commands.argument("x", IntegerArgumentType.integer())
            .then(Commands.argument("z", IntegerArgumentType.integer())
            .executes(this::set)
                .then(Commands.argument("players", ArgumentTypes.players())
                    .requires(sender -> pluginEnabled() &&
                        sender.getSender().hasPermission(CoordinateOffsetPermission.SET_OTHERS.node))
                    .executes(this::set)
            ))));

        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(root.build());
        });
    }

    private int reload(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        boolean success = CoordinateOffsetCore.get().reloadConfig();
        if (success) {
            sender.sendMessage(Component.text("CoordinateOffset configuration reloaded from file.").color(NamedTextColor.GREEN));
            sender.sendMessage(Component.text("Changes to offset providers may not apply until players quit and rejoin.").color(NamedTextColor.GRAY));
        } else {
            sender.sendMessage(Component.text("Failed to reload CoordinateOffset configuration from file. Check the console for details.").color(NamedTextColor.RED));
        }
        return Command.SINGLE_SUCCESS;
    }

    private int querySelf(CommandContext<CommandSourceStack> context) {
        // Explicitly check permission. The root command "/offset" has wider permission requirements.
        if (!context.getSource().getSender().hasPermission(CoordinateOffsetPermission.QUERY_SELF.node)) {
            context.getSource().getSender().sendMessage(Bukkit.permissionMessage());
            return 0;
        }

        if (!(context.getSource().getSender() instanceof Player player)) {
            context.getSource().getSender().sendMessage(Component.text("You must be a player to query your own offset."));
            return 0;
        }

        CreatedOffset offset = CoordinateOffsetCore.get().getOffsetHolder().getOffset(new PaperOffsetPlayer(player));

        if (offset.offset().equals(Offset.ZERO)) {
            context.getSource().getSender().sendMessage(Component
                .text("You have no coordinate offset applied. The coordinates you see are the real coordinates of the world.")
                .color(NamedTextColor.GREEN));
        } else {
            context.getSource().getSender().sendMessage(Component.text("Your coordinate offset is: ")
                .append(formatOffset(offset.offset()))
                .color(NamedTextColor.GRAY));
            context.getSource().getSender().sendMessage(Component.text("Your real coordinates are: ")
                .append(formatLocation(player.getLocation()))
                .color(NamedTextColor.GRAY));
        }

        if (context.getSource().getSender().hasPermission(CoordinateOffsetPermission.QUERY_VERBOSE.node)) {
            context.getSource().getSender().sendMessage(getQueryProviderMessage(offset));
        }

        return Command.SINGLE_SUCCESS;
    }

    private int queryOther(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        PlayerSelectorArgumentResolver targetResolver = context.getArgument("player", PlayerSelectorArgumentResolver.class);
        Player target = targetResolver.resolve(context.getSource()).getFirst();
        if (target == null) {
            return 0;
        }
        CreatedOffset offset = CoordinateOffsetCore.get().getOffsetHolder().getOffset(new PaperOffsetPlayer(target));

        context.getSource().getSender().sendMessage(Component.empty()
            .append(formatPlayerName(target))
            .append(Component.text("'s coordinate offset is: "))
            .append(formatOffset(offset.offset()))
            .color(NamedTextColor.GRAY));
        context.getSource().getSender().sendMessage(Component.empty()
            .append(formatPlayerName(target))
            .append(Component.text("'s real coordinates are: "))
            .append(formatLocation(target.getLocation()))
            .color(NamedTextColor.GRAY));

        if (context.getSource().getSender().hasPermission(CoordinateOffsetPermission.QUERY_VERBOSE.node)) {
            context.getSource().getSender().sendMessage(getQueryProviderMessage(offset));
        }

        return Command.SINGLE_SUCCESS;
    }

    private Component getQueryProviderMessage(CreatedOffset offset) {
        var provider = Component.text();
        provider.color(NamedTextColor.GRAY);
        provider.decorate(TextDecoration.ITALIC);
        switch (offset.source()) {
            case CreatedOffset.Source.PermissionBypass b -> {
                provider.append(Component.text("This offset was generated due to the permission "));
                provider.append(Component.text(b.perm().node).color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
            }
            case CreatedOffset.Source.Provider p -> {
                provider.append(Component.text("This offset was generated by provider "));
                provider.append(Component.text("\"" + p.provider().name + "\"").color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
                if (p.isOverride()) {
                    provider.append(Component.text(" as part of an offset provider override rule."));
                } else {
                    provider.append(Component.text(", the default offset provider."));
                }
            }
            case CreatedOffset.Source.SetCommand s -> {
                provider.append(Component.text("This offset was set by a command sent by "));
                provider.append(Component.text(s.sender()).color(PLAYER_NAME_COLOR).decoration(TextDecoration.ITALIC, false));
                provider.append(Component.text("."));
            }
        }
        return provider.build();
    }

    private int regenerate(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        List<Player> targets;
        try {
            PlayerSelectorArgumentResolver targetResolver = context.getArgument("players", PlayerSelectorArgumentResolver.class);
            targets = targetResolver.resolve(context.getSource());
        } catch (IllegalArgumentException e) {
            if (!(context.getSource().getSender() instanceof Player player)) {
                context.getSource().getSender().sendMessage(Component.text("You must be a player to regenerate your own offset."));
                return 0;
            }
            targets = List.of(player);
        }
        if (targets.stream().anyMatch(target -> !target.equals(context.getSource().getSender()) &&
            !context.getSource().getSender().hasPermission(CoordinateOffsetPermission.REGENERATE_OTHERS.node))) {
            context.getSource().getSender().sendMessage(Bukkit.permissionMessage());
            return 0;
        }

        context.getSource().getSender().sendMessage(Component.text("Regenerated coordinate offset for ")
            .color(NamedTextColor.GRAY)
            .append(formatPlayerNames(targets))
            .append(Component.text("."))
        );

        for (Player target : targets) {
            PaperLocation location = new PaperLocation(target.getLocation());
            PaperOffsetPlayer offsetPlayer = new PaperOffsetPlayer(target);
            boolean changed = CoordinateOffsetCore.get().getOffsetHolder().generateNextOffset(new OffsetProviderContext(
                offsetPlayer,
                location,
                location,
                CoordinateOffsetCore.get().getOffsetHolder().getOffset(offsetPlayer).offset(),
                OffsetProviderContext.ProvideReason.COMMAND_REGENERATE
            ));

            if (changed) {
                OffsetSwapHelpers.forceOffsetSwap(target);
            }
        }

        return Command.SINGLE_SUCCESS;
    }

    private int set(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        List<Player> targets;
        try {
            PlayerSelectorArgumentResolver targetResolver = context.getArgument("players", PlayerSelectorArgumentResolver.class);
            targets = targetResolver.resolve(context.getSource());
        } catch (IllegalArgumentException e) {
            if (!(context.getSource().getSender() instanceof Player player)) {
                context.getSource().getSender().sendMessage(Component.text("You must be a player to set your own offset."));
                return 0;
            }
            targets = List.of(player);
        }
        if (targets.stream().anyMatch(target -> !target.equals(context.getSource().getSender()) &&
            !context.getSource().getSender().hasPermission(CoordinateOffsetPermission.SET_OTHERS.node))) {
            context.getSource().getSender().sendMessage(Bukkit.permissionMessage());
            return 0;
        }

        int x = IntegerArgumentType.getInteger(context, "x");
        int z = IntegerArgumentType.getInteger(context, "z");

        Offset offset;
        try {
            offset = new Offset(x, z);
        } catch (IllegalArgumentException e) {
            context.getSource().getSender().sendMessage(Component.text("Invalid offset: " + e.getMessage()).color(NamedTextColor.RED));
            return 0;
        }

        context.getSource().getSender().sendMessage(Component.text("Set coordinate offset for ")
            .color(NamedTextColor.GRAY)
            .append(formatPlayerNames(targets))
            .append(Component.text(" to "))
            .append(formatOffset(offset))
            .append(Component.text("."))
        );

        for (Player target : targets) {
            OffsetPlayer player = new PaperOffsetPlayer(target);
            boolean changed = CoordinateOffsetCore.get().getOffsetHolder().setNextOffset(
                target.getUniqueId(),
                new CreatedOffset(
                    offset,
                    new CreatedOffset.Source.SetCommand(context.getSource().getSender().getName()),
                    player,
                    player.getLocation().getWorld(),
                    null
                )
            );

            if (changed) {
                OffsetSwapHelpers.forceOffsetSwap(target);
            }
        }

        return Command.SINGLE_SUCCESS;
    }

    private Component formatOffset(Offset offset) {
        return Component.text("[x=")
            .append(Component.text(offset.x()).color(NamedTextColor.YELLOW))
            .append(Component.text(", z="))
            .append(Component.text(offset.z()).color(NamedTextColor.YELLOW))
            .append(Component.text("]"))
            .color(NamedTextColor.DARK_AQUA);
    }

    private Component formatLocation(Location location) {
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
    private Component formatPlayerName(Player player) {
        return Component.text(player.getName())
            .color(PLAYER_NAME_COLOR)
            .hoverEvent(Component.text(player.getUniqueId().toString()));
    }

    private Component formatPlayerNames(List<Player> players) {
        if (players.size() == 1) {
            return formatPlayerName(players.getFirst());
        } else {
            return Component.text(players.size() + " players")
                .color(TextColor.color(0x18a9ff))
                .hoverEvent(Component.text(players.stream()
                    .map(Player::getName)
                    .collect(Collectors.joining(", "))));
        }
    }

    private boolean pluginEnabled() {
        return plugin != null && plugin.isEnabled();
    }
}
