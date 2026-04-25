package com.jarvis.deploy.resume;

import com.jarvis.deploy.checkpoint.CheckpointManager;
import com.jarvis.deploy.checkpoint.DeploymentCheckpoint;
import com.jarvis.deploy.deployment.DeploymentException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Manages resumption of interrupted deployments from their last saved checkpoint.
 */
public class DeploymentResumeManager {

    private final CheckpointManager checkpointManager;
    private final Map<String, ResumeRecord> resumeRecords = new HashMap<>();

    public DeploymentResumeManager(CheckpointManager checkpointManager) {
        if (checkpointManager == null) {
            throw new IllegalArgumentException("CheckpointManager must not be null");
        }
        this.checkpointManager = checkpointManager;
    }

    /**
     * Attempts to resume a deployment from its last checkpoint.
     *
     * @param deploymentId the deployment to resume
     * @return the checkpoint to resume from
     * @throws DeploymentException if no checkpoint is available or resumption is not allowed
     */
    public DeploymentCheckpoint resume(String deploymentId) throws DeploymentException {
        if (deploymentId == null || deploymentId.isBlank()) {
            throw new IllegalArgumentException("Deployment ID must not be blank");
        }

        Optional<DeploymentCheckpoint> checkpoint = checkpointManager.getLatestCheckpoint(deploymentId);
        if (checkpoint.isEmpty()) {
            throw new DeploymentException("No checkpoint found for deployment: " + deploymentId);
        }

        ResumeRecord record = resumeRecords.getOrDefault(deploymentId, new ResumeRecord(deploymentId));
        if (record.getAttempts() >= 3) {
            throw new DeploymentException("Max resume attempts (3) exceeded for deployment: " + deploymentId);
        }

        record.incrementAttempts();
        record.setLastResumedAt(Instant.now());
        resumeRecords.put(deploymentId, record);

        return checkpoint.get();
    }

    /**
     * Marks a deployment as successfully resumed, resetting attempt tracking.
     *
     * @param deploymentId the deployment that completed successfully after resume
     */
    public void markResumed(String deploymentId) {
        resumeRecords.remove(deploymentId);
    }

    /**
     * Returns whether a deployment is eligible for resumption.
     *
     * @param deploymentId the deployment ID to check
     * @return true if a checkpoint exists and attempts are within limit
     */
    public boolean isResumable(String deploymentId) {
        boolean hasCheckpoint = checkpointManager.getLatestCheckpoint(deploymentId).isPresent();
        int attempts = resumeRecords.getOrDefault(deploymentId, new ResumeRecord(deploymentId)).getAttempts();
        return hasCheckpoint && attempts < 3;
    }

    /**
     * Returns the resume record for a given deployment, if any.
     */
    public Optional<ResumeRecord> getResumeRecord(String deploymentId) {
        return Optional.ofNullable(resumeRecords.get(deploymentId));
    }
}
