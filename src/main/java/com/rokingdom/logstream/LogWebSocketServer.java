package com.rokingdom.logstream;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LogWebSocketServer extends WebSocketServer {

    private String authToken;
    private int maxConnections;
    private int backlogSize;

    private final Deque<String> backlog = new ArrayDeque<>();
    private final Set<WebSocket> authenticated = ConcurrentHashMap.newKeySet();

    public LogWebSocketServer(int port, String authToken, int maxConnections, int backlogSize) {
        super(new InetSocketAddress(port));
        this.authToken = authToken;
        this.maxConnections = maxConnections;
        this.backlogSize = backlogSize;
        setReuseAddr(true);
    }

    public void updateConfig(String authToken, int maxConnections, int backlogSize) {
        this.authToken = authToken;
        this.maxConnections = maxConnections;
        this.backlogSize = backlogSize;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        if (maxConnections > 0 && getConnections().size() > maxConnections) {
            conn.close(1013, "Server is at maximum capacity.");
            return;
        }

        if (authToken == null || authToken.isEmpty()) {
            authenticated.add(conn);
            sendBacklog(conn);
        }
        // If auth is enabled, wait for the client to send the token
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        if (!authenticated.contains(conn)) {
            if (authToken != null && !authToken.isEmpty() && message.trim().equals(authToken)) {
                authenticated.add(conn);
                sendBacklog(conn);
            } else {
                conn.close(1008, "Invalid authentication token.");
            }
        }
        // Authenticated clients don't need to send anything else
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        authenticated.remove(conn);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        if (conn != null) {
            authenticated.remove(conn);
        }
    }

    @Override
    public void onStart() {
        // Server started
    }

    public void broadcast(String line) {
        synchronized (backlog) {
            backlog.addLast(line);
            while (backlog.size() > backlogSize && backlogSize > 0) {
                backlog.removeFirst();
            }
        }

        for (WebSocket conn : authenticated) {
            if (conn.isOpen()) {
                conn.send(line);
            }
        }
    }

    private void sendBacklog(WebSocket conn) {
        if (backlogSize <= 0) return;
        synchronized (backlog) {
            for (String line : backlog) {
                conn.send(line);
            }
        }
    }

    public int getAuthenticatedCount() {
        return authenticated.size();
    }
}
