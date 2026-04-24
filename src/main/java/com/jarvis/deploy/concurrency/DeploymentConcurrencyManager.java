package com.jarvis.deploy.concurrency;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages concurrency constraints for deployments per environment.
 * Tracks active deployments and enforces policies before allowing new ones.
 */
public class DeploymentConcurrencyManager {

    private final Map<String, ConcurrencyPolicy> policies = new ConcurrentHashMap<>();
    // environment -> set of active deployment IDs
    private final Map<String, Set<String>> activeDeployments = new ConcurrentHashMap<>();
    // environment -> set of active app names
    private final Map<String, Set<String>> activeApps = new ConcurrentHashMap<>();

    public void registerPolicy(ConcurrencyPolicy policy) {
        if (policy == null) throw new IllegalArgumentException("Policy must not be null");
        policies.put(policy.getEnvironment(), policy);
        activeDeployments.putIfAbsent(policy.getEnvironment(), ConcurrentHashMap.newKeySet());
        activeApps.putIfAbsent(policy.getEnvironment(), ConcurrentHashMap.newKeySet());
    }

    /**
     * Attempts to acquire a deployment slot. Throws if the policy is violated.
     *
     * @param environment  target environment
     * @param deploymentId unique deployment ID
     * @param appName      application name
     */
    public void acquire(String environment, String deploymentId, String appName) {
        ConcurrencyPolicy policy = policies.get(environment);
        if (policy == null) {
            // No policy registered — allow freely
            activeDeployments.computeIfAbsent(environment, e -> ConcurrentHashMap.newKeySet()).add(deploymentId);
            activeApps.computeIfAbsent(environment, e -> ConcurrentHashMap.newKeySet()).add(appName);
            return;
        }

        Set<String> active = activeDeployments.computeIfAbsent(environment, e -> ConcurrentHashMap.newKeySet());
        Set<String> apps = activeApps.computeIfAbsent(environment, e -> ConcurrentHashMap.newKeySet());

        synchronized (this) {
            int current = active.size();
            if (current >= policy.getMaxConcurrentDeployments()) {
                throw new ConcurrencyViolationException(environment, appName, current,
                        policy.getMaxConcurrentDeployments());
            }
            if (!policy.isAllowSameAppConcurrency() && apps.contains(appName)) {
                throw new ConcurrencyViolationException(environment, appName, current,
                        policy.getMaxConcurrentDeployments());
            }
            active.add(deploymentId);
            apps.add(appName);
        }
    }

    /**
     * Releases a previously acquired deployment slot.
     */
    public void release(String environment, String deploymentId, String appName) {
        Set<String> active = activeDeployments.get(environment);
        Set<String> apps = activeApps.get(environment);
        if (active != null) active.remove(deploymentId);
        if (apps != null) apps.remove(appName);
    }

    public int getActiveCount(String environment) {
        Set<String> active = activeDeployments.get(environment);
        return active == null ? 0 : active.size();
    }

    public boolean hasPolicy(String environment) {
        return policies.containsKey(environment);
    }

    public ConcurrencyPolicy getPolicy(String environment) {
        return policies.get(environment);
    }
}
