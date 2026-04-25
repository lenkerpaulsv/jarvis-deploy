package com.jarvis.deploy.resume;

import java.time.Instant;

/**
 * Tracks resume attempt metadata for a specific deployment.
 */
public class ResumeRecord {

    private final String deploymentId;
    private int attempts;
    private Instant lastResumedAt;
    private final Instant createdAt;

    public ResumeRecord(String deploymentId) {
        if (deploymentId == null || deploymentId.isBlank()) {
            throw new IllegalArgumentException("Deployment ID must not be blank");
        }
        this.deploymentId = deploymentId;
        this.attempts = 0;
        this.createdAt = Instant.now();
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public int getAttempts() {
        return attempts;
    }

    public void incrementAttempts() {
        this.attempts++;
    }

    public Instant getLastResumedAt() {
        return lastResumedAt;
    }

    public void setLastResumedAt(Instant lastResumedAt) {
        this.lastResumedAt = lastResumedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "ResumeRecord{" +
                "deploymentId='" + deploymentId + '\'' +
                ", attempts=" + attempts +
                ", lastResumedAt=" + lastResumedAt +
                ", createdAt=" + createdAt +
                '}';
    }
}
