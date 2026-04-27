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

    /**
     * Returns whether this record has ever had a resume attempt.
     *
     * @return true if at least one resume attempt has been recorded
     */
    public boolean hasBeenAttempted() {
        return this.attempts > 0;
    }

    /**
     * Records a new resume attempt by incrementing the attempt counter
     * and updating the lastResumedAt timestamp to the current time.
     */
    public void recordAttempt() {
        this.attempts++;
        this.lastResumedAt = Instant.now();
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
