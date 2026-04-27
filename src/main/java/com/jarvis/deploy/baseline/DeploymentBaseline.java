package com.jarvis.deploy.baseline;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a captured baseline state of a deployment environment,
 * used for drift detection and compliance checks.
 */
public class DeploymentBaseline {

    private final String baselineId;
    private final String environment;
    private final String version;
    private final Instant capturedAt;
    private final Map<String, String> properties;
    private boolean locked;

    public DeploymentBaseline(String baselineId, String environment, String version,
                               Instant capturedAt, Map<String, String> properties) {
        this.baselineId = Objects.requireNonNull(baselineId, "baselineId must not be null");
        this.environment = Objects.requireNonNull(environment, "environment must not be null");
        this.version = Objects.requireNonNull(version, "version must not be null");
        this.capturedAt = Objects.requireNonNull(capturedAt, "capturedAt must not be null");
        this.properties = new HashMap<>(Objects.requireNonNull(properties, "properties must not be null"));
        this.locked = false;
    }

    public String getBaselineId() { return baselineId; }
    public String getEnvironment() { return environment; }
    public String getVersion() { return version; }
    public Instant getCapturedAt() { return capturedAt; }
    public Map<String, String> getProperties() { return Collections.unmodifiableMap(properties); }
    public boolean isLocked() { return locked; }

    public void lock() {
        this.locked = true;
    }

    public boolean hasDriftFrom(DeploymentBaseline other) {
        if (other == null) return true;
        return !this.properties.equals(other.properties) || !this.version.equals(other.version);
    }

    @Override
    public String toString() {
        return String.format("DeploymentBaseline{id='%s', env='%s', version='%s', capturedAt=%s, locked=%s}",
                baselineId, environment, version, capturedAt, locked);
    }
}
