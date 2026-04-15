package com.jarvis.deploy.metrics;

import java.time.Instant;

/**
 * Immutable snapshot of deployment metrics at a point in time.
 */
public class MetricsSnapshot {

    private final String environment;
    private final int totalDeployments;
    private final int successfulDeployments;
    private final int failedDeployments;
    private final int rollbacks;
    private final double successRate;
    private final long averageDeploymentMillis;
    private final Instant capturedAt;

    public MetricsSnapshot(DeploymentMetrics metrics) {
        this.environment = metrics.getEnvironment();
        this.totalDeployments = metrics.getTotalDeployments();
        this.successfulDeployments = metrics.getSuccessfulDeployments();
        this.failedDeployments = metrics.getFailedDeployments();
        this.rollbacks = metrics.getRollbacks();
        this.successRate = metrics.getSuccessRate();
        this.averageDeploymentMillis = metrics.getAverageDeploymentMillis();
        this.capturedAt = Instant.now();
    }

    public String getEnvironment() { return environment; }
    public int getTotalDeployments() { return totalDeployments; }
    public int getSuccessfulDeployments() { return successfulDeployments; }
    public int getFailedDeployments() { return failedDeployments; }
    public int getRollbacks() { return rollbacks; }
    public double getSuccessRate() { return successRate; }
    public long getAverageDeploymentMillis() { return averageDeploymentMillis; }
    public Instant getCapturedAt() { return capturedAt; }

    @Override
    public String toString() {
        return String.format(
            "MetricsSnapshot[env=%s, total=%d, success=%d, failed=%d, rollbacks=%d, successRate=%.1f%%, avgMs=%d, at=%s]",
            environment, totalDeployments, successfulDeployments, failedDeployments,
            rollbacks, successRate, averageDeploymentMillis, capturedAt
        );
    }
}
