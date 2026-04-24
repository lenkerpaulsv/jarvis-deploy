package com.jarvis.deploy.quota;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a deployment quota configuration for an environment.
 */
public class DeploymentQuota {

    private final String environment;
    private final int maxDeploymentsPerHour;
    private final int maxConcurrentDeployments;
    private final int maxDailyDeployments;
    private final Instant createdAt;

    public DeploymentQuota(String environment, int maxDeploymentsPerHour,
                           int maxConcurrentDeployments, int maxDailyDeployments) {
        if (environment == null || environment.isBlank()) {
            throw new IllegalArgumentException("Environment must not be blank");
        }
        if (maxDeploymentsPerHour <= 0 || maxConcurrentDeployments <= 0 || maxDailyDeployments <= 0) {
            throw new IllegalArgumentException("Quota limits must be positive");
        }
        this.environment = environment;
        this.maxDeploymentsPerHour = maxDeploymentsPerHour;
        this.maxConcurrentDeployments = maxConcurrentDeployments;
        this.maxDailyDeployments = maxDailyDeployments;
        this.createdAt = Instant.now();
    }

    public String getEnvironment() {
        return environment;
    }

    public int getMaxDeploymentsPerHour() {
        return maxDeploymentsPerHour;
    }

    public int getMaxConcurrentDeployments() {
        return maxConcurrentDeployments;
    }

    public int getMaxDailyDeployments() {
        return maxDailyDeployments;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeploymentQuota)) return false;
        DeploymentQuota that = (DeploymentQuota) o;
        return Objects.equals(environment, that.environment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(environment);
    }

    @Override
    public String toString() {
        return "DeploymentQuota{environment='" + environment + "', hourly=" + maxDeploymentsPerHour
                + ", concurrent=" + maxConcurrentDeployments + ", daily=" + maxDailyDeployments + "}";
    }
}
