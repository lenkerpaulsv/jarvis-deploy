package com.jarvis.deploy.audit;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Immutable value object representing a single audit log entry.
 */
public class AuditEntry {

    private final String action;
    private final String environment;
    private final String version;
    private final String performedBy;
    private final LocalDateTime timestamp;

    public AuditEntry(String action, String environment, String version, String performedBy, LocalDateTime timestamp) {
        this.action = Objects.requireNonNull(action, "action must not be null");
        this.environment = Objects.requireNonNull(environment, "environment must not be null");
        this.version = Objects.requireNonNull(version, "version must not be null");
        this.performedBy = Objects.requireNonNull(performedBy, "performedBy must not be null");
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp must not be null");
    }

    public String getAction() { return action; }
    public String getEnvironment() { return environment; }
    public String getVersion() { return version; }
    public String getPerformedBy() { return performedBy; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return String.format("AuditEntry{action='%s', env='%s', version='%s', by='%s', at=%s}",
                action, environment, version, performedBy, timestamp);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuditEntry)) return false;
        AuditEntry that = (AuditEntry) o;
        return action.equals(that.action) && environment.equals(that.environment)
                && version.equals(that.version) && performedBy.equals(that.performedBy)
                && timestamp.equals(that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(action, environment, version, performedBy, timestamp);
    }
}
