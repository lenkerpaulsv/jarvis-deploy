package com.jarvis.deploy.filter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class DeploymentFilter {

    private String environment;
    private String artifactId;
    private String status;
    private Instant from;
    private Instant to;
    private List<String> tags = new ArrayList<>();

    private DeploymentFilter() {}

    public static Builder builder() {
        return new Builder();
    }

    public String getEnvironment() { return environment; }
    public String getArtifactId() { return artifactId; }
    public String getStatus() { return status; }
    public Instant getFrom() { return from; }
    public Instant getTo() { return to; }
    public List<String> getTags() { return tags; }

    public boolean matches(String env, String artifact, String stat, Instant timestamp, List<String> deployTags) {
        if (environment != null && !environment.equalsIgnoreCase(env)) return false;
        if (artifactId != null && !artifactId.equalsIgnoreCase(artifact)) return false;
        if (status != null && !status.equalsIgnoreCase(stat)) return false;
        if (from != null && timestamp != null && timestamp.isBefore(from)) return false;
        if (to != null && timestamp != null && timestamp.isAfter(to)) return false;
        if (!tags.isEmpty() && (deployTags == null || deployTags.stream().noneMatch(tags::contains))) return false;
        return true;
    }

    public static class Builder {
        private final DeploymentFilter filter = new DeploymentFilter();

        public Builder environment(String env) { filter.environment = env; return this; }
        public Builder artifactId(String id) { filter.artifactId = id; return this; }
        public Builder status(String status) { filter.status = status; return this; }
        public Builder from(Instant from) { filter.from = from; return this; }
        public Builder to(Instant to) { filter.to = to; return this; }
        public Builder tag(String tag) { filter.tags.add(tag); return this; }
        public Builder tags(List<String> tags) { filter.tags.addAll(tags); return this; }
        public DeploymentFilter build() { return filter; }
    }
}
