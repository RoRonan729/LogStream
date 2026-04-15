package com.rokingdom.logstream;

import com.rokingdom.logstream.commands.LogStreamCommand;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public class LogStreamPlugin extends JavaPlugin {

    private LogWebSocketServer webSocketServer;
    private LogAppender logAppender;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        int port = getConfig().getInt("websocket-port", 8085);
        String authToken = getConfig().getString("auth-token", "");
        int maxConnections = getConfig().getInt("max-connections", 20);
        int backlogSize = getConfig().getInt("backlog-size", 50);
        Set<String> allowedLevels = new HashSet<>(getConfig().getStringList("allowed-levels"));

        webSocketServer = new LogWebSocketServer(port, authToken, maxConnections, backlogSize);
        webSocketServer.start();
        getLogger().info("LogStream WebSocket server started on port " + port);

        logAppender = new LogAppender(webSocketServer, allowedLevels);
        logAppender.start();

        Logger rootLogger = (Logger) LogManager.getRootLogger();
        rootLogger.addAppender(logAppender);

        getCommand("logstream").setExecutor(new LogStreamCommand(this, webSocketServer));
    }

    @Override
    public void onDisable() {
        if (logAppender != null) {
            logAppender.stop();
            Logger rootLogger = (Logger) LogManager.getRootLogger();
            rootLogger.removeAppender(logAppender);
        }

        if (webSocketServer != null) {
            try {
                webSocketServer.stop(1000);
                getLogger().info("LogStream WebSocket server stopped.");
            } catch (InterruptedException e) {
                getLogger().warning("WebSocket server interrupted during shutdown.");
                Thread.currentThread().interrupt();
            }
        }
    }

    public void reloadPlugin() {
        reloadConfig();

        String authToken = getConfig().getString("auth-token", "");
        int maxConnections = getConfig().getInt("max-connections", 20);
        int backlogSize = getConfig().getInt("backlog-size", 50);
        Set<String> allowedLevels = new HashSet<>(getConfig().getStringList("allowed-levels"));

        webSocketServer.updateConfig(authToken, maxConnections, backlogSize);
        logAppender.setAllowedLevels(allowedLevels);

        getLogger().info("LogStream configuration reloaded.");
    }
}
