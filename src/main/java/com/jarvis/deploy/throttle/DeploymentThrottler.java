package com.jarvis.deploy.throttle;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Throttles deployments per environment by enforcing a maximum number of
 * deployments within a sliding time window.
 */
public class DeploymentThrottler {

    private final int maxDeployments;
    private final long windowMillis;
    private final Map<String, Deque<Instant>> deploymentTimestamps = new ConcurrentHashMap<>();

    public DeploymentThrottler(int maxDeployments, long windowMillis) {
        if (maxDeployments <= 0) {
            throw new IllegalArgumentException("maxDeployments must be positive");
        }
        if (windowMillis <= 0) {
            throw new IllegalArgumentException("windowMillis must be positive");
        }
        this.maxDeployments = maxDeployments;
        this.windowMillis = windowMillis;
    }

    /**
     * Attempts to acquire a throttle slot for the given environment.
     *
     * @param environment the target environment
     * @return true if the deployment is allowed, false if throttled
     */
    public synchronized boolean tryAcquire(String environment) {
        if (environment == null || environment.isBlank()) {
            throw new IllegalArgumentException("environment must not be null or blank");
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
     * Returns the number of deployments recorded within the current window for the environment.
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
     * Resets throttle state for the given environment.
     */
    public synchronized void reset(String environment) {
        deploymentTimestamps.remove(environment);
    }

    private void evictExpired(Deque<Instant> timestamps, Instant now) {
        Instant cutoff = now.minusMillis(windowMillis);
        while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(cutoff)) {
            timestamps.pollFirst();
        }
    }
}
