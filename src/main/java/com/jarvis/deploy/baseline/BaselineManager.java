package com.jarvis.deploy.baseline;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages deployment baselines per environment, supporting capture,
 * retrieval, locking, and drift comparison.
 */
public class BaselineManager {

    private final Map<String, DeploymentBaseline> baselines = new ConcurrentHashMap<>();

    public DeploymentBaseline capture(String environment, String version, Map<String, String> properties) {
        Objects.requireNonNull(environment, "environment must not be null");
        Objects.requireNonNull(version, "version must not be null");
        Objects.requireNonNull(properties, "properties must not be null");

        String id = UUID.randomUUID().toString();
        DeploymentBaseline baseline = new DeploymentBaseline(id, environment, version, Instant.now(), properties);
        baselines.put(environment, baseline);
        return baseline;
    }

    public Optional<DeploymentBaseline> getBaseline(String environment) {
        return Optional.ofNullable(baselines.get(environment));
    }

    public boolean hasBaseline(String environment) {
        return baselines.containsKey(environment);
    }

    public void lockBaseline(String environment) {
        DeploymentBaseline baseline = baselines.get(environment);
        if (baseline == null) {
            throw new BaselineException("No baseline found for environment: " + environment);
        }
        baseline.lock();
    }

    public BaselineDriftReport checkDrift(String environment, String currentVersion,
                                          Map<String, String> currentProperties) {
        Optional<DeploymentBaseline> opt = getBaseline(environment);
        if (opt.isEmpty()) {
            throw new BaselineException("No baseline captured for environment: " + environment);
        }
        DeploymentBaseline baseline = opt.get();
        DeploymentBaseline current = new DeploymentBaseline(
                "current", environment, currentVersion, Instant.now(), currentProperties);

        boolean hasDrift = baseline.hasDriftFrom(current);
        Map<String, String[]> driftedKeys = new LinkedHashMap<>();

        for (Map.Entry<String, String> entry : baseline.getProperties().entrySet()) {
            String key = entry.getKey();
            String baselineVal = entry.getValue();
            String currentVal = currentProperties.get(key);
            if (!Objects.equals(baselineVal, currentVal)) {
                driftedKeys.put(key, new String[]{baselineVal, currentVal});
            }
        }
        for (String key : currentProperties.keySet()) {
            if (!baseline.getProperties().containsKey(key)) {
                driftedKeys.put(key, new String[]{null, currentProperties.get(key)});
            }
        }
        return new BaselineDriftReport(environment, baseline.getVersion(), currentVersion, hasDrift, driftedKeys);
    }

    public void removeBaseline(String environment) {
        baselines.remove(environment);
    }

    public int count() {
        return baselines.size();
    }
}
