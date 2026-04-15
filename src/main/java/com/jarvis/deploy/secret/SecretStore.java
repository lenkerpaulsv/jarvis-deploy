package com.jarvis.deploy.secret;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory store for deployment secrets, keyed by environment and secret key.
 * Secrets are stored masked and resolved only when explicitly requested.
 */
public class SecretStore {

    // Map<environment, Map<key, SecretEntry>>
    private final Map<String, Map<String, SecretEntry>> store = new ConcurrentHashMap<>();

    public void put(SecretEntry entry) {
        Objects.requireNonNull(entry, "SecretEntry must not be null");
        store.computeIfAbsent(entry.getEnvironment(), e -> new ConcurrentHashMap<>())
             .put(entry.getKey(), entry);
    }

    public Optional<SecretEntry> get(String environment, String key) {
        if (environment == null || key == null) return Optional.empty();
        Map<String, SecretEntry> envSecrets = store.get(environment);
        if (envSecrets == null) return Optional.empty();
        return Optional.ofNullable(envSecrets.get(key));
    }

    public boolean remove(String environment, String key) {
        Map<String, SecretEntry> envSecrets = store.get(environment);
        if (envSecrets == null) return false;
        return envSecrets.remove(key) != null;
    }

    public Map<String, SecretEntry> getAllForEnvironment(String environment) {
        return Collections.unmodifiableMap(
            store.getOrDefault(environment, Collections.emptyMap())
        );
    }

    public boolean containsKey(String environment, String key) {
        return store.getOrDefault(environment, Collections.emptyMap()).containsKey(key);
    }

    public void clear(String environment) {
        store.remove(environment);
    }

    public int size(String environment) {
        return store.getOrDefault(environment, Collections.emptyMap()).size();
    }
}
