package com.jarvis.deploy.metrics;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tracks deployment metrics such as counts, durations, and success/failure rates.
 */
public class DeploymentMetrics {

    private final AtomicInteger totalDeployments = new AtomicInteger(0);
    private final AtomicInteger successfulDeployments = new AtomicInteger(0);
    private final AtomicInteger failedDeployments = new AtomicInteger(0);
    private final AtomicInteger rollbacks = new AtomicInteger(0);
    private final AtomicLong totalDeploymentMillis = new AtomicLong(0);

    private final String environment;

    public DeploymentMetrics(String environment) {
        if (environment == null || environment.isBlank()) {
            throw new IllegalArgumentException("Environment must not be null or blank");
        }
        this.environment = environment;
    }

    public void recordDeployment(boolean success, Instant start, Instant end) {
        totalDeployments.incrementAndGet();
        long millis = Duration.between(start, end).toMillis();
        totalDeploymentMillis.addAndGet(millis);
        if (success) {
            successfulDeployments.incrementAndGet();
        } else {
            failedDeployments.incrementAndGet();
        }
    }

    public void recordRollback() {
        rollbacks.incrementAndGet();
    }

    public int getTotalDeployments() {
        return totalDeployments.get();
    }

    public int getSuccessfulDeployments() {
        return successfulDeployments.get();
    }

    public int getFailedDeployments() {
        return failedDeployments.get();
    }

    public int getRollbacks() {
        return rollbacks.get();
    }

    public double getSuccessRate() {
        int total = totalDeployments.get();
        if (total == 0) return 0.0;
        return (double) successfulDeployments.get() / total * 100.0;
    }

    public long getAverageDeploymentMillis() {
        int total = totalDeployments.get();
        if (total == 0) return 0L;
        return totalDeploymentMillis.get() / total;
    }

    public String getEnvironment() {
        return environment;
    }
}
