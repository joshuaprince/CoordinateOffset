package com.jtprince.coordinateoffset;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@SuppressWarnings("deprecation")
class CoordinateOffsetCommands {
    private final CoordinateOffset plugin;
    private final ComponentBuilder prefix = new ComponentBuilder("[CoordinateOffset] ").color(ChatColor.AQUA);

    public CoordinateOffsetCommands(CoordinateOffset plugin) {
        this.plugin = plugin;
    }

    public class OffsetCommand implements CommandExecutor {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                 @NotNull String cmd, @NotNull String[] args) {
            // Base permission checked by command in plugin.yml
            Player target;
            String pronoun;
            if (args.length == 0) {
                if (!(sender instanceof Player)) {
                    replyError(sender, "You must be a player or specify a player to query offsets.");
                    return true;
                }
                target = (Player) sender;
                pronoun = "your";
            } else {
                if (!sender.hasPermission(CoordinateOffsetPermissions.QUERY_OTHERS)) {
                    sender.sendMessage(Objects.requireNonNull(command.permissionMessage()));
                    return true;
                }
                target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    replyError(sender, "Unknown player \"" + args[0] + "\".");
                    return true;
                }
                pronoun = target.getName() + "'s";
            }

            Offset offset = plugin.getPlayerManager().getOffset(target);

            sender.spigot().sendMessage(new ComponentBuilder(prefix)
                    .append("[x=" + offset.x() + ", z=" + offset.z() + "]").color(ChatColor.GOLD)
                    .append(" is " + pronoun + " current offset.").color(ChatColor.GREEN)
                    .create());

            Location real = target.getLocation();
            sender.spigot().sendMessage(new ComponentBuilder(prefix)
                    .append(String.format("(%.1f, %.1f, %.1f)", real.getX(), real.getY(), real.getZ())).color(ChatColor.YELLOW)
                    .append(" is " + pronoun + " real position in world ").color(ChatColor.GREEN)
                    .append(target.getWorld().getName()).color(ChatColor.YELLOW)
                    .append(".").color(ChatColor.GREEN)
                    .create());

            return true;
        }
    }

    public class OffsetReloadCommand implements CommandExecutor {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                 @NotNull String cmd, @NotNull String[] args) {
            // Base permission checked by command in plugin.yml
            try {
                plugin.reload();
                replyOk(sender, "Reloaded CoordinateOffset config. Players may need to relog to see the changes.");
            } catch (Exception e) {
                replyError(sender, "Failed to reload the config. Check the console for details.");
                e.printStackTrace();
            }

            return true;
        }
    }

    private void replyOk(CommandSender to, @SuppressWarnings("SameParameterValue") String message) {
        to.spigot().sendMessage(new ComponentBuilder(prefix).append(message).color(ChatColor.GREEN).create());
    }

    private void replyError(CommandSender to, String message) {
        to.spigot().sendMessage(new ComponentBuilder(prefix).append(message).color(ChatColor.RED).create());
    }
}
