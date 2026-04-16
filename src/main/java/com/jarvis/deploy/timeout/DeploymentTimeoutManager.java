package com.jarvis.deploy.timeout;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks and enforces deployment timeouts per environment.
 */
public class DeploymentTimeoutManager {

    private final Map<String, Duration> timeoutConfig = new ConcurrentHashMap<>();
    private final Map<String, Instant> startTimes = new ConcurrentHashMap<>();

    public static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(30);

    public void configureTimeout(String environment, Duration timeout) {
        if (environment == null || environment.isBlank()) {
            throw new IllegalArgumentException("Environment must not be blank");
        }
        if (timeout == null || timeout.isNegative() || timeout.isZero()) {
            throw new IllegalArgumentException("Timeout must be a positive duration");
        }
        timeoutConfig.put(environment, timeout);
    }

    public Duration getTimeout(String environment) {
        return timeoutConfig.getOrDefault(environment, DEFAULT_TIMEOUT);
    }

    public void startTracking(String deploymentId) {
        if (deploymentId == null || deploymentId.isBlank()) {
            throw new IllegalArgumentException("Deployment ID must not be blank");
        }
        startTimes.put(deploymentId, Instant.now());
    }

    public boolean isTimedOut(String deploymentId, String environment) {
        Instant start = startTimes.get(deploymentId);
        if (start == null) {
            throw new IllegalStateException("No tracking started for deployment: " + deploymentId);
        }
        Duration elapsed = Duration.between(start, Instant.now());
        return elapsed.compareTo(getTimeout(environment)) > 0;
    }

    public Duration elapsed(String deploymentId) {
        Instant start = startTimes.get(deploymentId);
        if (start == null) {
            throw new IllegalStateException("No tracking started for deployment: " + deploymentId);
        }
        return Duration.between(start, Instant.now());
    }

    public void stopTracking(String deploymentId) {
        startTimes.remove(deploymentId);
    }

    public boolean isTracking(String deploymentId) {
        return startTimes.containsKey(deploymentId);
    }
}
