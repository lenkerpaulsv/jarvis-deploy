package com.jarvis.deploy.snapshot;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Captures a point-in-time snapshot of a deployment's configuration and state,
 * enabling rollback and audit comparisons.
 */
public class DeploymentSnapshot {

    private final String snapshotId;
    private final String environment;
    private final String artifactVersion;
    private final Instant capturedAt;
    private final Map<String, String> configProperties;
    private final String deployedBy;

    public DeploymentSnapshot(String snapshotId,
                               String environment,
                               String artifactVersion,
                               Instant capturedAt,
                               Map<String, String> configProperties,
                               String deployedBy) {
        this.snapshotId = Objects.requireNonNull(snapshotId, "snapshotId must not be null");
        this.environment = Objects.requireNonNull(environment, "environment must not be null");
        this.artifactVersion = Objects.requireNonNull(artifactVersion, "artifactVersion must not be null");
        this.capturedAt = Objects.requireNonNull(capturedAt, "capturedAt must not be null");
        this.configProperties = Collections.unmodifiableMap(
                new HashMap<>(Objects.requireNonNull(configProperties, "configProperties must not be null")));
        this.deployedBy = Objects.requireNonNull(deployedBy, "deployedBy must not be null");
    }

    public String getSnapshotId() {
        return snapshotId;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getArtifactVersion() {
        return artifactVersion;
    }

    public Instant getCapturedAt() {
        return capturedAt;
    }

    public Map<String, String> getConfigProperties() {
        return configProperties;
    }

    public String getDeployedBy() {
        return deployedBy;
    }

    /**
     * Returns a diff summary of config keys that differ between this snapshot and another.
     */
    public Map<String, String[]> diffConfig(DeploymentSnapshot other) {
        Map<String, String[]> diff = new HashMap<>();
        for (Map.Entry<String, String> entry : this.configProperties.entrySet()) {
            String otherValue = other.configProperties.get(entry.getKey());
            if (!entry.getValue().equals(otherValue)) {
                diff.put(entry.getKey(), new String[]{entry.getValue(), otherValue});
            }
        }
        for (String key : other.configProperties.keySet()) {
            if (!this.configProperties.containsKey(key)) {
                diff.put(key, new String[]{null, other.configProperties.get(key)});
            }
        }
        return diff;
    }

    @Override
    public String toString() {
        return "DeploymentSnapshot{" +
                "snapshotId='" + snapshotId + '\'' +
                ", environment='" + environment + '\'' +
                ", artifactVersion='" + artifactVersion + '\'' +
                ", capturedAt=" + capturedAt +
                ", deployedBy='" + deployedBy + '\'' +
                '}';
    }
}
