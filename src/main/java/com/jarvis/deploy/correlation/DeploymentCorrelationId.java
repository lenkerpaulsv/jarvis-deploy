package com.jarvis.deploy.correlation;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a unique correlation ID for tracing a deployment across components.
 */
public class DeploymentCorrelationId {

    private final String id;
    private final String environment;
    private final String artifactId;
    private final Instant createdAt;

    public DeploymentCorrelationId(String environment, String artifactId) {
        this.id = UUID.randomUUID().toString();
        this.environment = Objects.requireNonNull(environment, "environment must not be null");
        this.artifactId = Objects.requireNonNull(artifactId, "artifactId must not be null");
        this.createdAt = Instant.now();
    }

    // For deserialization / reconstruction
    public DeploymentCorrelationId(String id, String environment, String artifactId, Instant createdAt) {
        this.id = Objects.requireNonNull(id);
        this.environment = Objects.requireNonNull(environment);
        this.artifactId = Objects.requireNonNull(artifactId);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public String getId() { return id; }
    public String getEnvironment() { return environment; }
    public String getArtifactId() { return artifactId; }
    public Instant getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return String.format("[%s] env=%s artifact=%s at=%s", id, environment, artifactId, createdAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeploymentCorrelationId)) return false;
        DeploymentCorrelationId that = (DeploymentCorrelationId) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
