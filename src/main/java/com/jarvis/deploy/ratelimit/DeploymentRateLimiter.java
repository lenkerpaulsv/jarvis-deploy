package com.jarvis.deploy.ratelimit;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiter that restricts the number of deployments per environment
 * within a configurable sliding time window.
 */
public class DeploymentRateLimiter {

    private final int maxDeployments;
    private final long windowSeconds;
    private final Map<String, Deque<Instant>> deploymentTimestamps = new ConcurrentHashMap<>();

    public DeploymentRateLimiter(int maxDeployments, long windowSeconds) {
        if (maxDeployments <= 0) {
            throw new IllegalArgumentException("maxDeployments must be positive");
        }
        if (windowSeconds <= 0) {
            throw new IllegalArgumentException("windowSeconds must be positive");
        }
        this.maxDeployments = maxDeployments;
        this.windowSeconds = windowSeconds;
    }

    /**
     * Attempts to acquire a deployment slot for the given environment.
     *
     * @param environment the target environment name
     * @return true if the deployment is allowed, false if rate limit exceeded
     */
    public synchronized boolean tryAcquire(String environment) {
        if (environment == null || environment.isBlank()) {
            throw new IllegalArgumentException("Environment must not be null or blank");
        }
        Instant now = Instant.now();
        Deque<Instant> timestamps = deploymentTimestamps.computeIfAbsent(environment, e -> new ArrayDeque<>());
        evictExpired(timestamps, now);
        if (timestamps.size() >= maxDeployments) {
            return false;
        }
        timestamps.addLast(now);
        return true;
    }

    /**
     * Returns the number of deployments recorded in the current window for the given environment.
     */
    public synchronized int currentCount(String environment) {
        Deque<Instant> timestamps = deploymentTimestamps.get(environment);
        if (timestamps == null) {
            return 0;
        }
        evictExpired(timestamps, Instant.now());
        return timestamps.size();
    }

    /**
     * Resets all recorded timestamps for the given environment.
     */
    public synchronized void reset(String environment) {
        deploymentTimestamps.remove(environment);
    }

    private void evictExpired(Deque<Instant> timestamps, Instant now) {
        Instant cutoff = now.minusSeconds(windowSeconds);
        while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(cutoff)) {
            timestamps.pollFirst();
        }
    }

    public int getMaxDeployments() {
        return maxDeployments;
    }

    public long getWindowSeconds() {
        return windowSeconds;
    }
}
