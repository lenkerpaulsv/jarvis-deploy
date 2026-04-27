package com.jarvis.deploy.canary;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages canary deployments: creation, traffic adjustment, promotion, and abort.
 */
public class CanaryManager {

    private final Map<String, CanaryDeployment> canaries = new ConcurrentHashMap<>();

    public CanaryDeployment startCanary(String deploymentId, String environment, String version, int initialTrafficPct) {
        if (canaries.containsKey(deploymentId)) {
            throw new IllegalStateException("Canary deployment already exists for id: " + deploymentId);
        }
        CanaryDeployment canary = new CanaryDeployment(deploymentId, environment, version, initialTrafficPct);
        canaries.put(deploymentId, canary);
        return canary;
    }

    public CanaryDeployment adjustTraffic(String deploymentId, int newTrafficPct) {
        CanaryDeployment canary = getActiveCanary(deploymentId);
        canary.setTrafficPercentage(newTrafficPct);
        return canary;
    }

    public CanaryDeployment promote(String deploymentId) {
        CanaryDeployment canary = getActiveCanary(deploymentId);
        canary.promote();
        return canary;
    }

    public CanaryDeployment abort(String deploymentId) {
        CanaryDeployment canary = getActiveCanary(deploymentId);
        canary.abort();
        return canary;
    }

    public Optional<CanaryDeployment> find(String deploymentId) {
        return Optional.ofNullable(canaries.get(deploymentId));
    }

    public Collection<CanaryDeployment> listActive() {
        return canaries.values().stream()
                .filter(CanaryDeployment::isActive)
                .toList();
    }

    public Collection<CanaryDeployment> listAll() {
        return canaries.values();
    }

    private CanaryDeployment getActiveCanary(String deploymentId) {
        CanaryDeployment canary = canaries.get(deploymentId);
        if (canary == null) {
            throw new IllegalArgumentException("No canary deployment found for id: " + deploymentId);
        }
        if (!canary.isActive()) {
            throw new IllegalStateException("Canary deployment " + deploymentId + " is not active (status=" + canary.getStatus() + ")");
        }
        return canary;
    }
}
