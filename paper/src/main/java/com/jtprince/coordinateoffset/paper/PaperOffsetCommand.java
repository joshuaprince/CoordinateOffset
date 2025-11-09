package com.jtprince.coordinateoffset.paper;

import com.jtprince.coordinateoffset.CoordinateOffsetCore;
import com.jtprince.coordinateoffset.CoordinateOffsetPermission;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.command.*;
import com.jtprince.coordinateoffset.paper.adapter.PaperOffsetPlayer;
import com.jtprince.coordinateoffset.provider.OffsetProvider;
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
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
public class PaperOffsetCommand {
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
    private final CoordinateOffsetCore core;

    public PaperOffsetCommand(CoordinateOffsetPaperPlugin plugin, CoordinateOffsetCore core) {
        this.plugin = plugin;
        this.core = core;
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
            .then(Commands.argument("x", IntegerArgumentType.integer(-OffsetProvider.OFFSET_MAX, OffsetProvider.OFFSET_MAX))
            .then(Commands.argument("z", IntegerArgumentType.integer(-OffsetProvider.OFFSET_MAX, OffsetProvider.OFFSET_MAX))
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
        return toResult(core.getCommandExecutor().execute(new OffsetReloadCommand(toSender(context.getSource().getSender()))));
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

        boolean isVerbose = context.getSource().getSender().hasPermission(CoordinateOffsetPermission.QUERY_VERBOSE.node);
        OffsetQueryCommand offsetQueryCommand = new OffsetQueryCommand(
            toSender(context.getSource().getSender()),
            new PaperOffsetPlayer(player),
            isVerbose
        );
        return toResult(core.getCommandExecutor().execute(offsetQueryCommand));
    }

    private int queryOther(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        PlayerSelectorArgumentResolver targetResolver = context.getArgument("player", PlayerSelectorArgumentResolver.class);
        Player target = targetResolver.resolve(context.getSource()).getFirst();
        if (target == null) {
            return 0;
        }

        boolean isVerbose = context.getSource().getSender().hasPermission(CoordinateOffsetPermission.QUERY_VERBOSE.node);
        OffsetQueryCommand offsetQueryCommand = new OffsetQueryCommand(
            toSender(context.getSource().getSender()),
            new PaperOffsetPlayer(target),
            isVerbose
        );
        return toResult(core.getCommandExecutor().execute(offsetQueryCommand));
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

        OffsetRegenerateCommand offsetRegenerateCommand = new OffsetRegenerateCommand(
            toSender(context.getSource().getSender()),
            targets.stream().map(PaperOffsetPlayer::new).toList()
        );
        return toResult(core.getCommandExecutor().execute(offsetRegenerateCommand));
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

        OffsetSetCommand offsetSetCommand = new OffsetSetCommand(
            toSender(context.getSource().getSender()),
            targets.stream().map(PaperOffsetPlayer::new).toList(),
            offset
        );
        return toResult(core.getCommandExecutor().execute(offsetSetCommand));
    }

    private static OffsetCommandSender toSender(CommandSender sender) {
        return new OffsetCommandSender(sender, sender.getName(), sender instanceof Player ? new PaperOffsetPlayer((Player) sender) : null);
    }

    private static int toResult(OffsetCommandExecutor.Result result) {
        return switch (result) {
            case SUCCESS -> Command.SINGLE_SUCCESS;
            case FAIL -> 0;
        };
    }

    private boolean pluginEnabled() {
        return plugin != null && plugin.isEnabled();
    }
}
