package com.jarvis.deploy.report;

import java.time.Instant;

public class ReportEntry {
    private final String deploymentId;
    private final String artifactVersion;
    private final String status;
    private final Instant deployedAt;
    private final String deployedBy;
    private final long durationSeconds;

    public ReportEntry(String deploymentId, String artifactVersion, String status,
                       Instant deployedAt, String deployedBy, long durationSeconds) {
        this.deploymentId = deploymentId;
        this.artifactVersion = artifactVersion;
        this.status = status;
        this.deployedAt = deployedAt;
        this.deployedBy = deployedBy;
        this.durationSeconds = durationSeconds;
    }

    public String getDeploymentId() { return deploymentId; }
    public String getArtifactVersion() { return artifactVersion; }
    public String getStatus() { return status; }
    public Instant getDeployedAt() { return deployedAt; }
    public String getDeployedBy() { return deployedBy; }
    public long getDurationSeconds() { return durationSeconds; }

    @Override
    public String toString() {
        return "ReportEntry[" + deploymentId + ", " + artifactVersion + ", " + status + "]";
    }
}
