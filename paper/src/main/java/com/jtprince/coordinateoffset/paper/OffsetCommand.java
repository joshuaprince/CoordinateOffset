package com.jtprince.coordinateoffset.paper;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerPositionAndLook;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUnloadChunk;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateViewPosition;
import com.jtprince.coordinateoffset.CoordinateOffsetCore;
import com.jtprince.coordinateoffset.CoordinateOffsetPermission;
import com.jtprince.coordinateoffset.Offset;
import com.jtprince.coordinateoffset.paper.adapter.PaperLocation;
import com.jtprince.coordinateoffset.paper.adapter.PaperOffsetPlayer;
import com.jtprince.coordinateoffset.provider.OffsetProviderContext;
import com.mojang.brigadier.Command;
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
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
public class OffsetCommand {
    /** Players with any of these permissions can see and the root /offset command. */
    private static final Set<CoordinateOffsetPermission> ROOT_COMMAND_PERMS = Set.of(
        CoordinateOffsetPermission.QUERY_SELF,
        CoordinateOffsetPermission.QUERY_OTHERS,
        CoordinateOffsetPermission.RELOAD
    );

    private @Nullable CoordinateOffsetPaperPlugin plugin;

    public void registerCommands(CoordinateOffsetPaperPlugin plugin) {
        this.plugin = plugin;

        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("offset");

        // /offset - shortcut for /offset query
        // Permissions for /offset are checked in the executor for plain /offset
        root
            .requires(sender -> pluginEnabled() &&
                ROOT_COMMAND_PERMS.stream().anyMatch(p -> sender.getSender().hasPermission(p.node)))
            .executes(this::querySelf);

        // /offset query <player>
        root.then(Commands.literal("query")
            .requires(sender -> pluginEnabled() &&
                sender.getSender().hasPermission(CoordinateOffsetPermission.QUERY_SELF.node) ||
                    sender.getSender().hasPermission(CoordinateOffsetPermission.QUERY_OTHERS.node))
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

        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(root.build());
        });

        // /offset set
        root.then(Commands.literal("regenerate")
            .requires(sender -> pluginEnabled() &&
                sender.getSender().hasPermission(CoordinateOffsetPermission.QUERY_SELF.node)) // TODO
            .then(Commands.argument("player", ArgumentTypes.player())
                .executes(this::regenerate)));
    }

    private int regenerate(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSender sender = context.getSource().getSender();
        PlayerSelectorArgumentResolver targetResolver = context.getArgument("player", PlayerSelectorArgumentResolver.class);
        Player player = targetResolver.resolve(context.getSource()).getFirst();

        CoordinateOffsetCore.get().getOffsetHolder().generateNextOffset(new OffsetProviderContext(
            new PaperOffsetPlayer(player),
            player.getWorld().getName(),
            new PaperLocation(player.getLocation()),
            OffsetProviderContext.ProvideReason.DEATH_RESPAWN
        ));

        int cx = player.getChunk().getX();
        int cz = player.getChunk().getZ();
        List<Chunk> chunks = player.getSentChunks().stream()
            .sorted(Comparator.comparing(c -> ((c.getX() - cx) * (c.getX() - cx) + (c.getZ() - cz) * (c.getZ() - cz))))
            .toList();

        for (Chunk chunk : chunks.reversed()) {
            PacketEvents.getAPI().getPlayerManager().sendPacket(player,
                new WrapperPlayServerUnloadChunk(chunk.getX(), chunk.getZ()));
        }

        var l = player.getLocation();
        var pkt = new WrapperPlayServerPlayerPositionAndLook(
            l.x(), l.y(), l.z(), l.getYaw(), l.getPitch(), (byte) 0, 999, false);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, pkt);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player,
            new WrapperPlayServerUpdateViewPosition(player.getLocation().getChunk().getX(), player.getLocation().getChunk().getZ()));

        Set<Entity> alreadyReloadedEntities = new HashSet<>();
        for (Chunk chunk : chunks) {
            player.getWorld().refreshChunk(chunk.getX(), chunk.getZ());
            for (Entity entity : chunk.getEntities()) {
                if (entity.getTrackedBy().contains(player)) {
                    player.hideEntity(CoordinateOffsetPaperPlugin.getInstance(), entity);
                    player.showEntity(CoordinateOffsetPaperPlugin.getInstance(), entity);
                    alreadyReloadedEntities.add(entity);
                }
            }
        }
        for (Entity entity : player.getWorld().getEntities()) {
            if (entity.getTrackedBy().contains(player) && !alreadyReloadedEntities.contains(entity)) {
                player.hideEntity(CoordinateOffsetPaperPlugin.getInstance(), entity);
                player.showEntity(CoordinateOffsetPaperPlugin.getInstance(), entity);
            }
        }

        return Command.SINGLE_SUCCESS;
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

        Offset offset = CoordinateOffsetCore.get().getOffsetHolder().getOffset(new PaperOffsetPlayer(player));

        if (offset.equals(Offset.ZERO)) {
            context.getSource().getSender().sendMessage(Component
                .text("You have no coordinate offset applied. The coordinates you see are the real coordinates of the world.")
                .color(NamedTextColor.GREEN));
            return Command.SINGLE_SUCCESS;
        }

        context.getSource().getSender().sendMessage(Component.text("Your coordinate offset is: ")
            .append(formatOffset(offset))
            .color(NamedTextColor.GRAY));
        context.getSource().getSender().sendMessage(Component.text("Your real coordinates are: ")
            .append(formatLocation(player.getLocation()))
            .color(NamedTextColor.GRAY));

        return Command.SINGLE_SUCCESS;
    }

    private int queryOther(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        PlayerSelectorArgumentResolver targetResolver = context.getArgument("player", PlayerSelectorArgumentResolver.class);
        Player target = targetResolver.resolve(context.getSource()).getFirst();
        if (target == null) {
            return 0;
        }
        Offset offset = CoordinateOffsetCore.get().getOffsetHolder().getOffset(new PaperOffsetPlayer(target));

        context.getSource().getSender().sendMessage(Component.empty()
            .append(Component.text(target.getName())
                .color(NamedTextColor.BLUE))
            .append(Component.text("'s coordinate offset is: "))
            .append(formatOffset(offset))
            .color(NamedTextColor.GRAY));
        context.getSource().getSender().sendMessage(Component.empty()
            .append(Component.text(target.getName())
                .color(NamedTextColor.BLUE))
            .append(Component.text("'s real coordinates are: "))
            .append(formatLocation(target.getLocation()))
            .color(NamedTextColor.GRAY));

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

    private boolean pluginEnabled() {
        return plugin != null && plugin.isEnabled();
    }
}
