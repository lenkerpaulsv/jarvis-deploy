package com.jarvis.deploy.quota;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages deployment quotas per environment, enforcing hourly, daily,
 * and concurrency limits.
 */
public class DeploymentQuotaManager {

    private final Map<String, DeploymentQuota> quotas = new ConcurrentHashMap<>();
    private final Map<String, List<Instant>> deploymentTimestamps = new ConcurrentHashMap<>();
    private final Map<String, Integer> activeDeployments = new ConcurrentHashMap<>();

    public void registerQuota(DeploymentQuota quota) {
        Objects.requireNonNull(quota, "Quota must not be null");
        quotas.put(quota.getEnvironment(), quota);
    }

    public boolean hasQuota(String environment) {
        return quotas.containsKey(environment);
    }

    public DeploymentQuota getQuota(String environment) {
        return quotas.get(environment);
    }

    /**
     * Checks whether a new deployment is permitted under the registered quota.
     *
     * @throws QuotaExceededException if any quota limit is breached
     */
    public void checkAndReserve(String environment) {
        DeploymentQuota quota = quotas.get(environment);
        if (quota == null) {
            return; // no quota configured — allow
        }

        Instant now = Instant.now();
        List<Instant> timestamps = deploymentTimestamps
                .computeIfAbsent(environment, k -> new ArrayList<>());

        // Purge timestamps older than 24 hours
        timestamps.removeIf(t -> t.isBefore(now.minus(24, ChronoUnit.HOURS)));

        long lastHourCount = timestamps.stream()
                .filter(t -> t.isAfter(now.minus(1, ChronoUnit.HOURS)))
                .count();

        if (lastHourCount >= quota.getMaxDeploymentsPerHour()) {
            throw new QuotaExceededException(environment, "hourly",
                    quota.getMaxDeploymentsPerHour());
        }

        if (timestamps.size() >= quota.getMaxDailyDeployments()) {
            throw new QuotaExceededException(environment, "daily",
                    quota.getMaxDailyDeployments());
        }

        int active = activeDeployments.getOrDefault(environment, 0);
        if (active >= quota.getMaxConcurrentDeployments()) {
            throw new QuotaExceededException(environment, "concurrent",
                    quota.getMaxConcurrentDeployments());
        }

        timestamps.add(now);
        activeDeployments.merge(environment, 1, Integer::sum);
    }

    public void releaseActive(String environment) {
        activeDeployments.computeIfPresent(environment,
                (k, v) -> v > 1 ? v - 1 : null);
    }

    public int getActiveCount(String environment) {
        return activeDeployments.getOrDefault(environment, 0);
    }

    public void removeQuota(String environment) {
        quotas.remove(environment);
        deploymentTimestamps.remove(environment);
        activeDeployments.remove(environment);
    }
}
