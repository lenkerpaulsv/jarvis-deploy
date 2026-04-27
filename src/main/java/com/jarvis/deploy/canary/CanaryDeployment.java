package com.jarvis.deploy.canary;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a canary deployment configuration and its current state.
 */
public class CanaryDeployment {

    private final String deploymentId;
    private final String environment;
    private final String version;
    private int trafficPercentage;
    private CanaryStatus status;
    private final Instant createdAt;
    private Instant promotedAt;

    public CanaryDeployment(String deploymentId, String environment, String version, int trafficPercentage) {
        if (trafficPercentage < 1 || trafficPercentage > 100) {
            throw new IllegalArgumentException("Traffic percentage must be between 1 and 100");
        }
        this.deploymentId = Objects.requireNonNull(deploymentId, "deploymentId must not be null");
        this.environment = Objects.requireNonNull(environment, "environment must not be null");
        this.version = Objects.requireNonNull(version, "version must not be null");
        this.trafficPercentage = trafficPercentage;
        this.status = CanaryStatus.ACTIVE;
        this.createdAt = Instant.now();
    }

    public String getDeploymentId() { return deploymentId; }
    public String getEnvironment() { return environment; }
    public String getVersion() { return version; }
    public int getTrafficPercentage() { return trafficPercentage; }
    public CanaryStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getPromotedAt() { return promotedAt; }

    public void setTrafficPercentage(int trafficPercentage) {
        if (trafficPercentage < 1 || trafficPercentage > 100) {
            throw new IllegalArgumentException("Traffic percentage must be between 1 and 100");
        }
        this.trafficPercentage = trafficPercentage;
    }

    public void promote() {
        this.status = CanaryStatus.PROMOTED;
        this.promotedAt = Instant.now();
    }

    public void abort() {
        this.status = CanaryStatus.ABORTED;
    }

    public boolean isActive() {
        return status == CanaryStatus.ACTIVE;
    }

    @Override
    public String toString() {
        return String.format("CanaryDeployment{id='%s', env='%s', version='%s', traffic=%d%%, status=%s}",
                deploymentId, environment, version, trafficPercentage, status);
    }
}
