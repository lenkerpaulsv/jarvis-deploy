package com.jarvis.deploy.baseline;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable report describing configuration drift between a captured
 * baseline and the current deployment state.
 */
public class BaselineDriftReport {

    private final String environment;
    private final String baselineVersion;
    private final String currentVersion;
    private final boolean driftDetected;
    // key -> [baselineValue, currentValue]
    private final Map<String, String[]> driftedKeys;

    public BaselineDriftReport(String environment, String baselineVersion, String currentVersion,
                                boolean driftDetected, Map<String, String[]> driftedKeys) {
        this.environment = Objects.requireNonNull(environment);
        this.baselineVersion = Objects.requireNonNull(baselineVersion);
        this.currentVersion = Objects.requireNonNull(currentVersion);
        this.driftDetected = driftDetected;
        this.driftedKeys = Collections.unmodifiableMap(Objects.requireNonNull(driftedKeys));
    }

    public String getEnvironment() { return environment; }
    public String getBaselineVersion() { return baselineVersion; }
    public String getCurrentVersion() { return currentVersion; }
    public boolean isDriftDetected() { return driftDetected; }
    public Map<String, String[]> getDriftedKeys() { return driftedKeys; }

    public boolean isVersionChanged() {
        return !baselineVersion.equals(currentVersion);
    }

    public String getSummary() {
        if (!driftDetected) {
            return String.format("[%s] No drift detected (version=%s)", environment, currentVersion);
        }
        return String.format("[%s] Drift detected: version %s -> %s, %d key(s) changed",
                environment, baselineVersion, currentVersion, driftedKeys.size());
    }

    @Override
    public String toString() {
        return getSummary();
    }
}
