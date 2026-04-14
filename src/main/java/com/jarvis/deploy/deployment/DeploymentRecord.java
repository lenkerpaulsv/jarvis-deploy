package com.jarvis.deploy.deployment;

import java.time.LocalDateTime;

/**
 * Represents a single deployment record used for tracking and rollback support.
 */
public class DeploymentRecord {

    private final String version;
    private final String environment;
    private final LocalDateTime deployedAt;
    private final String artifactPath;
    private DeploymentStatus status;

    public DeploymentRecord(String version, String environment, String artifactPath) {
        this.version = version;
        this.environment = environment;
        this.artifactPath = artifactPath;
        this.deployedAt = LocalDateTime.now();
        this.status = DeploymentStatus.IN_PROGRESS;
    }

    public String getVersion() {
        return version;
    }

    public String getEnvironment() {
        return environment;
    }

    public LocalDateTime getDeployedAt() {
        return deployedAt;
    }

    public String getArtifactPath() {
        return artifactPath;
    }

    public DeploymentStatus getStatus() {
        return status;
    }

    public void setStatus(DeploymentStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return String.format("DeploymentRecord{version='%s', environment='%s', deployedAt=%s, status=%s}",
                version, environment, deployedAt, status);
    }

    public enum DeploymentStatus {
        IN_PROGRESS,
        SUCCESS,
        FAILED,
        ROLLED_BACK
    }
}
