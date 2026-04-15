package com.jarvis.deploy.schedule;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a scheduled deployment entry with environment, artifact, and timing info.
 */
public class DeploymentSchedule {

    private final String scheduleId;
    private final String environment;
    private final String artifactPath;
    private final Instant scheduledAt;
    private ScheduleStatus status;

    public DeploymentSchedule(String scheduleId, String environment, String artifactPath, Instant scheduledAt) {
        if (scheduleId == null || scheduleId.isBlank()) throw new IllegalArgumentException("scheduleId must not be blank");
        if (environment == null || environment.isBlank()) throw new IllegalArgumentException("environment must not be blank");
        if (artifactPath == null || artifactPath.isBlank()) throw new IllegalArgumentException("artifactPath must not be blank");
        Objects.requireNonNull(scheduledAt, "scheduledAt must not be null");

        this.scheduleId = scheduleId;
        this.environment = environment;
        this.artifactPath = artifactPath;
        this.scheduledAt = scheduledAt;
        this.status = ScheduleStatus.PENDING;
    }

    public String getScheduleId() { return scheduleId; }
    public String getEnvironment() { return environment; }
    public String getArtifactPath() { return artifactPath; }
    public Instant getScheduledAt() { return scheduledAt; }
    public ScheduleStatus getStatus() { return status; }

    public void setStatus(ScheduleStatus status) {
        Objects.requireNonNull(status, "status must not be null");
        this.status = status;
    }

    public boolean isDue(Instant now) {
        return !scheduledAt.isAfter(now) && status == ScheduleStatus.PENDING;
    }

    @Override
    public String toString() {
        return String.format("DeploymentSchedule{id='%s', env='%s', artifact='%s', at=%s, status=%s}",
                scheduleId, environment, artifactPath, scheduledAt, status);
    }
}
