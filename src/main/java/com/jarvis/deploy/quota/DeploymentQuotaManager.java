package com.jarvis.deploy.quota;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages per-environment deployment quotas to prevent runaway deployments.
 */
public class DeploymentQuotaManager {

    private final Map<String, Integer> quotaLimits = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> usageCounts = new ConcurrentHashMap<>();
    private final int defaultLimit;

    public DeploymentQuotaManager(int defaultLimit) {
        if (defaultLimit <= 0) throw new IllegalArgumentException("Default limit must be positive");
        this.defaultLimit = defaultLimit;
    }

    public void setQuota(String environment, int limit) {
        if (environment == null || environment.isBlank()) throw new IllegalArgumentException("Environment must not be blank");
        if (limit <= 0) throw new IllegalArgumentException("Limit must be positive");
        quotaLimits.put(environment, limit);
    }

    public int getQuota(String environment) {
        return quotaLimits.getOrDefault(environment, defaultLimit);
    }

    public int getUsage(String environment) {
        return usageCounts.getOrDefault(environment, new AtomicInteger(0)).get();
    }

    public boolean tryConsume(String environment) {
        int limit = getQuota(environment);
        AtomicInteger counter = usageCounts.computeIfAbsent(environment, e -> new AtomicInteger(0));
        int current = counter.get();
        if (current >= limit) return false;
        return counter.compareAndSet(current, current + 1);
    }

    public void reset(String environment) {
        usageCounts.remove(environment);
    }

    public void resetAll() {
        usageCounts.clear();
    }

    public boolean isExceeded(String environment) {
        return getUsage(environment) >= getQuota(environment);
    }
}
