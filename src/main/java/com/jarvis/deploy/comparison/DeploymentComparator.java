package com.jarvis.deploy.comparison;

import com.jarvis.deploy.deployment.DeploymentRecord;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Compares two deployment records and produces a structured summary of differences.
 * Useful for auditing, reporting, and pre-promotion analysis.
 */
public class DeploymentComparator {

    /**
     * Compares two DeploymentRecord instances and returns a ComparisonResult
     * describing what changed between them.
     *
     * @param baseline the earlier or reference deployment
     * @param candidate the newer or target deployment
     * @return a ComparisonResult summarising all detected differences
     */
    public ComparisonResult compare(DeploymentRecord baseline, DeploymentRecord candidate) {
        Objects.requireNonNull(baseline, "baseline must not be null");
        Objects.requireNonNull(candidate, "candidate must not be null");

        List<ComparisonDelta> deltas = new ArrayList<>();

        // Version change
        if (!Objects.equals(baseline.getVersion(), candidate.getVersion())) {
            deltas.add(new ComparisonDelta("version", baseline.getVersion(), candidate.getVersion()));
        }

        // Environment change
        if (!Objects.equals(baseline.getEnvironment(), candidate.getEnvironment())) {
            deltas.add(new ComparisonDelta("environment", baseline.getEnvironment(), candidate.getEnvironment()));
        }

        // Status change
        if (!Objects.equals(baseline.getStatus(), candidate.getStatus())) {
            deltas.add(new ComparisonDelta("status",
                    baseline.getStatus() != null ? baseline.getStatus().name() : null,
                    candidate.getStatus() != null ? candidate.getStatus().name() : null));
        }

        // Deployed-by change
        if (!Objects.equals(baseline.getDeployedBy(), candidate.getDeployedBy())) {
            deltas.add(new ComparisonDelta("deployedBy", baseline.getDeployedBy(), candidate.getDeployedBy()));
        }

        // Artifact change
        if (!Objects.equals(baseline.getArtifactPath(), candidate.getArtifactPath())) {
            deltas.add(new ComparisonDelta("artifactPath", baseline.getArtifactPath(), candidate.getArtifactPath()));
        }

        // Metadata key-level comparison
        Map<String, String> baseMeta = baseline.getMetadata();
        Map<String, String> candMeta = candidate.getMetadata();
        if (baseMeta != null || candMeta != null) {
            Map<String, String> safeBase = baseMeta != null ? baseMeta : Map.of();
            Map<String, String> safeCand = candMeta != null ? candMeta : Map.of();
            detectMetadataDeltas(safeBase, safeCand, deltas);
        }

        // Deployment duration change (if timestamps available)
        Duration baseDuration = resolveDuration(baseline);
        Duration candDuration = resolveDuration(candidate);
        if (baseDuration != null && candDuration != null && !baseDuration.equals(candDuration)) {
            deltas.add(new ComparisonDelta("deploymentDuration",
                    baseDuration.toSeconds() + "s",
                    candDuration.toSeconds() + "s"));
        }

        return new ComparisonResult(baseline.getDeploymentId(), candidate.getDeploymentId(), deltas);
    }

    /**
     * Detects added, removed, and modified metadata keys between two snapshots.
     */
    private void detectMetadataDeltas(Map<String, String> base,
                                       Map<String, String> candidate,
                                       List<ComparisonDelta> deltas) {
        // Modified or removed keys
        for (Map.Entry<String, String> entry : base.entrySet()) {
            String key = entry.getKey();
            if (!candidate.containsKey(key)) {
                deltas.add(new ComparisonDelta("metadata." + key, entry.getValue(), null));
            } else if (!Objects.equals(entry.getValue(), candidate.get(key))) {
                deltas.add(new ComparisonDelta("metadata." + key, entry.getValue(), candidate.get(key)));
            }
        }
        // Added keys
        for (Map.Entry<String, String> entry : candidate.entrySet()) {
            if (!base.containsKey(entry.getKey())) {
                deltas.add(new ComparisonDelta("metadata." + entry.getKey(), null, entry.getValue()));
            }
        }
    }

    /**
     * Attempts to derive a deployment duration from a record's start/end timestamps.
     * Returns null if the record does not carry sufficient timestamp data.
     */
    private Duration resolveDuration(DeploymentRecord record) {
        if (record.getStartedAt() != null && record.getCompletedAt() != null) {
            return Duration.between(record.getStartedAt(), record.getCompletedAt());
        }
        return null;
    }
}
