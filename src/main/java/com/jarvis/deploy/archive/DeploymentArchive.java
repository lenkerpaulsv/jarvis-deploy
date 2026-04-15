package com.jarvis.deploy.archive;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents an archived deployment artifact with metadata.
 */
public class DeploymentArchive {

    private final String archiveId;
    private final String environment;
    private final String artifactPath;
    private final String version;
    private final Instant archivedAt;
    private final Map<String, String> metadata;

    public DeploymentArchive(String archiveId, String environment,
                             String artifactPath, String version,
                             Instant archivedAt, Map<String, String> metadata) {
        this.archiveId = Objects.requireNonNull(archiveId, "archiveId must not be null");
        this.environment = Objects.requireNonNull(environment, "environment must not be null");
        this.artifactPath = Objects.requireNonNull(artifactPath, "artifactPath must not be null");
        this.version = Objects.requireNonNull(version, "version must not be null");
        this.archivedAt = Objects.requireNonNull(archivedAt, "archivedAt must not be null");
        this.metadata = metadata != null ? Collections.unmodifiableMap(new HashMap<>(metadata)) : Collections.emptyMap();
    }

    public String getArchiveId() { return archiveId; }
    public String getEnvironment() { return environment; }
    public String getArtifactPath() { return artifactPath; }
    public String getVersion() { return version; }
    public Instant getArchivedAt() { return archivedAt; }
    public Map<String, String> getMetadata() { return metadata; }

    @Override
    public String toString() {
        return String.format("DeploymentArchive{id='%s', env='%s', version='%s', archivedAt=%s}",
                archiveId, environment, version, archivedAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeploymentArchive)) return false;
        DeploymentArchive that = (DeploymentArchive) o;
        return Objects.equals(archiveId, that.archiveId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(archiveId);
    }
}
