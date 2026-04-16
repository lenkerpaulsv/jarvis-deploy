package com.jarvis.deploy.alert;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class AlertEvent {
    private final String eventId;
    private final AlertRule rule;
    private final String environment;
    private final String message;
    private final Instant timestamp;

    public AlertEvent(AlertRule rule, String environment, String message) {
        this.eventId = UUID.randomUUID().toString();
        this.rule = Objects.requireNonNull(rule);
        this.environment = Objects.requireNonNull(environment);
        this.message = Objects.requireNonNull(message);
        this.timestamp = Instant.now();
    }

    public String getEventId() { return eventId; }
    public AlertRule getRule() { return rule; }
    public String getEnvironment() { return environment; }
    public String getMessage() { return message; }
    public Instant getTimestamp() { return timestamp; }
    public AlertSeverity getSeverity() { return rule.getSeverity(); }

    @Override
    public String toString() {
        return "AlertEvent{id='" + eventId + "', rule='" + rule.getName() + "', env='" + environment + "', severity=" + getSeverity() + "}";
    }
}
