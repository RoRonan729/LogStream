package com.rokingdom.logstream;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.util.Set;

public class LogAppender extends AbstractAppender {

    private final LogWebSocketServer webSocketServer;
    private Set<String> allowedLevels;

    public LogAppender(LogWebSocketServer webSocketServer, Set<String> allowedLevels) {
        super("LogStreamAppender", null, PatternLayout.newBuilder()
                .withPattern("[%d{HH:mm:ss}] [%t/%level]: %msg%n")
                .build(), false, Property.EMPTY_ARRAY);
        this.webSocketServer = webSocketServer;
        this.allowedLevels = allowedLevels;
    }

    public void setAllowedLevels(Set<String> allowedLevels) {
        this.allowedLevels = allowedLevels;
    }

    @Override
    public void append(LogEvent event) {
        if (!allowedLevels.contains(event.getLevel().name())) {
            return;
        }

        String formatted = getLayout().toSerializable(event).toString();
        webSocketServer.broadcast(formatted.trim());
    }
}
