package com.jarvis.deploy.metrics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages per-environment DeploymentMetrics and produces snapshots for reporting.
 */
public class MetricsReporter {

    private final Map<String, DeploymentMetrics> metricsMap = new ConcurrentHashMap<>();

    public DeploymentMetrics getOrCreate(String environment) {
        return metricsMap.computeIfAbsent(environment, DeploymentMetrics::new);
    }

    public MetricsSnapshot snapshot(String environment) {
        DeploymentMetrics metrics = metricsMap.get(environment);
        if (metrics == null) {
            throw new IllegalArgumentException("No metrics found for environment: " + environment);
        }
        return new MetricsSnapshot(metrics);
    }

    public void printReport(String environment) {
        MetricsSnapshot snap = snapshot(environment);
        System.out.println("=== Deployment Metrics Report ===");
        System.out.println("Environment       : " + snap.getEnvironment());
        System.out.println("Total Deployments : " + snap.getTotalDeployments());
        System.out.println("Successful        : " + snap.getSuccessfulDeployments());
        System.out.println("Failed            : " + snap.getFailedDeployments());
        System.out.println("Rollbacks         : " + snap.getRollbacks());
        System.out.printf ("Success Rate      : %.1f%%%n", snap.getSuccessRate());
        System.out.println("Avg Duration (ms) : " + snap.getAverageDeploymentMillis());
        System.out.println("Captured At       : " + snap.getCapturedAt());
        System.out.println("=================================");
    }

    public boolean hasMetrics(String environment) {
        return metricsMap.containsKey(environment);
    }

    public void reset(String environment) {
        metricsMap.remove(environment);
    }
}
