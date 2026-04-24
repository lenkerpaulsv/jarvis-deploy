package com.jarvis.deploy.drain;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages graceful draining of active deployments before shutdown or rollback.
 * Tracks in-flight deployments and waits for them to complete within a timeout.
 */
public class DeploymentDrainManager {

    private final Map<String, Instant> activeDeployments = new ConcurrentHashMap<>();
    private final AtomicBoolean draining = new AtomicBoolean(false);
    private final Duration drainTimeout;

    public DeploymentDrainManager(Duration drainTimeout) {
        if (drainTimeout == null || drainTimeout.isNegative()) {
            throw new IllegalArgumentException("Drain timeout must be a positive duration");
        }
        this.drainTimeout = drainTimeout;
    }

    public void register(String deploymentId) {
        if (draining.get()) {
            throw new IllegalStateException("Cannot register new deployment while draining: " + deploymentId);
        }
        if (deploymentId == null || deploymentId.isBlank()) {
            throw new IllegalArgumentException("Deployment ID must not be null or blank");
        }
        activeDeployments.put(deploymentId, Instant.now());
    }

    public void complete(String deploymentId) {
        activeDeployments.remove(deploymentId);
    }

    public DrainResult drain() throws InterruptedException {
        draining.set(true);
        Instant deadline = Instant.now().plus(drainTimeout);

        while (!activeDeployments.isEmpty()) {
            if (Instant.now().isAfter(deadline)) {
                return DrainResult.timedOut(activeDeployments.keySet().stream().toList());
            }
            Thread.sleep(100);
        }

        return DrainResult.success();
    }

    public boolean isDraining() {
        return draining.get();
    }

    public int activeCount() {
        return activeDeployments.size();
    }

    public void reset() {
        draining.set(false);
        activeDeployments.clear();
    }
}
