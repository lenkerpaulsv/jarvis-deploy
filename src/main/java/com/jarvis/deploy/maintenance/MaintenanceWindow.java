package com.jarvis.deploy.maintenance;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a scheduled maintenance window during which deployments are blocked.
 */
public class MaintenanceWindow {

    private final String id;
    private final String environment;
    private final Instant start;
    private final Instant end;
    private final String reason;
    private boolean active;

    public MaintenanceWindow(String id, String environment, Instant start, Instant end, String reason) {
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("Maintenance window end must be after start");
        }
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.environment = Objects.requireNonNull(environment, "environment must not be null");
        this.start = Objects.requireNonNull(start, "start must not be null");
        this.end = Objects.requireNonNull(end, "end must not be null");
        this.reason = reason != null ? reason : "";
        this.active = true;
    }

    public boolean isActiveAt(Instant moment) {
        return active && !moment.isBefore(start) && moment.isBefore(end);
    }

    public void cancel() {
        this.active = false;
    }

    public String getId() { return id; }
    public String getEnvironment() { return environment; }
    public Instant getStart() { return start; }
    public Instant getEnd() { return end; }
    public String getReason() { return reason; }
    public boolean isActive() { return active; }

    @Override
    public String toString() {
        return String.format("MaintenanceWindow{id='%s', env='%s', start=%s, end=%s, active=%b}",
                id, environment, start, end, active);
    }
}
