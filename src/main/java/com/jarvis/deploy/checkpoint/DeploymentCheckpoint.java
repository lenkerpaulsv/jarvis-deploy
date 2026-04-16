package com.jarvis.deploy.checkpoint;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DeploymentCheckpoint {

    private final String checkpointId;
    private final String deploymentId;
    private final String stageName;
    private final Instant createdAt;
    private final Map<String, String> metadata;
    private CheckpointStatus status;

    public DeploymentCheckpoint(String deploymentId, String stageName) {
        this.checkpointId = UUID.randomUUID().toString();
        this.deploymentId = deploymentId;
        this.stageName = stageName;
        this.createdAt = Instant.now();
        this.metadata = new HashMap<>();
        this.status = CheckpointStatus.PENDING;
    }

    public String getCheckpointId() { return checkpointId; }
    public String getDeploymentId() { return deploymentId; }
    public String getStageName() { return stageName; }
    public Instant getCreatedAt() { return createdAt; }
    public CheckpointStatus getStatus() { return status; }
    public Map<String, String> getMetadata() { return Collections.unmodifiableMap(metadata); }

    public void setStatus(CheckpointStatus status) {
        if (status == null) throw new IllegalArgumentException("Status must not be null");
        this.status = status;
    }

    public void addMetadata(String key, String value) {
        if (key == null || key.isBlank()) throw new IllegalArgumentException("Metadata key must not be blank");
        metadata.put(key, value);
    }

    public boolean isCompleted() {
        return status == CheckpointStatus.COMPLETED;
    }

    @Override
    public String toString() {
        return "DeploymentCheckpoint{id='" + checkpointId + "', deployment='" + deploymentId +
               "', stage='" + stageName + "', status=" + status + "}";
    }
}
