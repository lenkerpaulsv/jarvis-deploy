package com.jarvis.deploy.checkpoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class CheckpointManager {

    private final Map<String, DeploymentCheckpoint> checkpoints = new ConcurrentHashMap<>();

    public DeploymentCheckpoint createCheckpoint(String deploymentId, String stageName) {
        if (deploymentId == null || deploymentId.isBlank())
            throw new IllegalArgumentException("Deployment ID must not be blank");
        if (stageName == null || stageName.isBlank())
            throw new IllegalArgumentException("Stage name must not be blank");
        DeploymentCheckpoint checkpoint = new DeploymentCheckpoint(deploymentId, stageName);
        checkpoints.put(checkpoint.getCheckpointId(), checkpoint);
        return checkpoint;
    }

    public Optional<DeploymentCheckpoint> findById(String checkpointId) {
        return Optional.ofNullable(checkpoints.get(checkpointId));
    }

    public List<DeploymentCheckpoint> findByDeploymentId(String deploymentId) {
        return checkpoints.values().stream()
                .filter(c -> c.getDeploymentId().equals(deploymentId))
                .collect(Collectors.toList());
    }

    public void markCompleted(String checkpointId) {
        DeploymentCheckpoint cp = checkpoints.get(checkpointId);
        if (cp == null) throw new IllegalArgumentException("Checkpoint not found: " + checkpointId);
        cp.setStatus(CheckpointStatus.COMPLETED);
    }

    public void markFailed(String checkpointId) {
        DeploymentCheckpoint cp = checkpoints.get(checkpointId);
        if (cp == null) throw new IllegalArgumentException("Checkpoint not found: " + checkpointId);
        cp.setStatus(CheckpointStatus.FAILED);
    }

    public boolean allCompleted(String deploymentId) {
        List<DeploymentCheckpoint> list = findByDeploymentId(deploymentId);
        return !list.isEmpty() && list.stream().allMatch(DeploymentCheckpoint::isCompleted);
    }

    public Optional<DeploymentCheckpoint> getLastFailedCheckpoint(String deploymentId) {
        return findByDeploymentId(deploymentId).stream()
                .filter(c -> c.getStatus() == CheckpointStatus.FAILED)
                .reduce((first, second) -> second);
    }

    public int getCheckpointCount() {
        return checkpoints.size();
    }
}
