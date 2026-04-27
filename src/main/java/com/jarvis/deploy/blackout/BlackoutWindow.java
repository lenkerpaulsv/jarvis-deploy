package com.jarvis.deploy.blackout;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a blackout window during which deployments are prohibited.
 */
public class BlackoutWindow {

    private final String id;
    private final String environment;
    private final LocalDateTime start;
    private final LocalDateTime end;
    private final String reason;

    public BlackoutWindow(String id, String environment, LocalDateTime start, LocalDateTime end, String reason) {
        if (end.isBefore(start) || end.isEqual(start)) {
            throw new IllegalArgumentException("Blackout end must be after start");
        }
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.environment = Objects.requireNonNull(environment, "environment must not be null");
        this.start = Objects.requireNonNull(start, "start must not be null");
        this.end = Objects.requireNonNull(end, "end must not be null");
        this.reason = reason != null ? reason : "";
    }

    public String getId() { return id; }
    public String getEnvironment() { return environment; }
    public LocalDateTime getStart() { return start; }
    public LocalDateTime getEnd() { return end; }
    public String getReason() { return reason; }

    public boolean isActive(LocalDateTime at) {
        return !at.isBefore(start) && at.isBefore(end);
    }

    @Override
    public String toString() {
        return String.format("BlackoutWindow{id='%s', env='%s', start=%s, end=%s, reason='%s'}",
                id, environment, start, end, reason);
    }
}
