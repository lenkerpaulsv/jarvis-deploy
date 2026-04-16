package com.jarvis.deploy.version;

import java.time.Instant;
import java.util.*;

/**
 * Tracks deployed versions per environment, supporting version history and latest lookup.
 */
public class VersionTracker {

    private final Map<String, Deque<VersionEntry>> envVersions = new HashMap<>();

    public void record(String environment, String version, String deployedBy) {
        if (environment == null || environment.isBlank()) throw new IllegalArgumentException("Environment must not be blank");
        if (version == null || version.isBlank()) throw new IllegalArgumentException("Version must not be blank");
        envVersions.computeIfAbsent(environment, e -> new ArrayDeque<>())
                   .addFirst(new VersionEntry(version, deployedBy, Instant.now()));
    }

    public Optional<VersionEntry> getLatest(String environment) {
        Deque<VersionEntry> entries = envVersions.get(environment);
        if (entries == null || entries.isEmpty()) return Optional.empty();
        return Optional.of(entries.peekFirst());
    }

    public List<VersionEntry> getHistory(String environment) {
        Deque<VersionEntry> entries = envVersions.get(environment);
        if (entries == null) return Collections.emptyList();
        return Collections.unmodifiableList(new ArrayList<>(entries));
    }

    public boolean hasEnvironment(String environment) {
        return envVersions.containsKey(environment) && !envVersions.get(environment).isEmpty();
    }

    public Set<String> trackedEnvironments() {
        return Collections.unmodifiableSet(envVersions.keySet());
    }

    public void clear(String environment) {
        envVersions.remove(environment);
    }
}
