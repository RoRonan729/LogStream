package com.rokingdom.logstream.commands;

import com.rokingdom.logstream.LogStreamPlugin;
import com.rokingdom.logstream.LogWebSocketServer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LogStreamCommand implements CommandExecutor, TabCompleter {

    private final LogStreamPlugin plugin;
    private final LogWebSocketServer webSocketServer;

    public LogStreamCommand(LogStreamPlugin plugin, LogWebSocketServer webSocketServer) {
        this.plugin = plugin;
        this.webSocketServer = webSocketServer;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.GOLD + "LogStream v" + plugin.getDescription().getVersion());
            sender.sendMessage(ChatColor.YELLOW + "Usage: /logstream <reload|status>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                plugin.reloadPlugin();
                sender.sendMessage(ChatColor.GREEN + "LogStream configuration reloaded.");
                break;
            case "status":
                int clients = webSocketServer.getAuthenticatedCount();
                int port = plugin.getConfig().getInt("websocket-port", 8085);
                sender.sendMessage(ChatColor.GOLD + "--- LogStream Status ---");
                sender.sendMessage(ChatColor.YELLOW + "WebSocket port: " + ChatColor.WHITE + port);
                sender.sendMessage(ChatColor.YELLOW + "Connected clients: " + ChatColor.WHITE + clients);
                sender.sendMessage(ChatColor.YELLOW + "Auth enabled: " + ChatColor.WHITE +
                        (!plugin.getConfig().getString("auth-token", "").isEmpty()));
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use /logstream <reload|status>");
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            for (String sub : Arrays.asList("reload", "status")) {
                if (sub.startsWith(args[0].toLowerCase())) {
                    completions.add(sub);
                }
            }
            return completions;
        }
        return List.of();
    }
}
