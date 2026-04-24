package com.jarvis.deploy.cache;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple TTL-based in-memory cache for deployment artifacts and metadata.
 * Entries expire after a configurable duration to avoid stale data.
 */
public class DeploymentCache {

    private final long ttlMillis;
    private final int maxSize;
    private final Map<String, CacheEntry> store;

    public DeploymentCache(long ttlMillis, int maxSize) {
        if (ttlMillis <= 0) throw new IllegalArgumentException("TTL must be positive");
        if (maxSize <= 0) throw new IllegalArgumentException("Max size must be positive");
        this.ttlMillis = ttlMillis;
        this.maxSize = maxSize;
        this.store = new ConcurrentHashMap<>();
    }

    public void put(String key, String value) {
        if (key == null || key.isBlank()) throw new IllegalArgumentException("Cache key must not be blank");
        evictExpired();
        if (store.size() >= maxSize) {
            evictOldest();
        }
        store.put(key, new CacheEntry(value, Instant.now()));
    }

    public Optional<String> get(String key) {
        CacheEntry entry = store.get(key);
        if (entry == null) return Optional.empty();
        if (isExpired(entry)) {
            store.remove(key);
            return Optional.empty();
        }
        return Optional.of(entry.value);
    }

    public void invalidate(String key) {
        store.remove(key);
    }

    public void clear() {
        store.clear();
    }

    public int size() {
        evictExpired();
        return store.size();
    }

    private boolean isExpired(CacheEntry entry) {
        return Instant.now().toEpochMilli() - entry.createdAt.toEpochMilli() > ttlMillis;
    }

    private void evictExpired() {
        store.entrySet().removeIf(e -> isExpired(e.getValue()));
    }

    private void evictOldest() {
        store.entrySet().stream()
            .min((a, b) -> a.getValue().createdAt.compareTo(b.getValue().createdAt))
            .map(Map.Entry::getKey)
            .ifPresent(store::remove);
    }

    private static class CacheEntry {
        final String value;
        final Instant createdAt;

        CacheEntry(String value, Instant createdAt) {
            this.value = value;
            this.createdAt = createdAt;
        }
    }
}
