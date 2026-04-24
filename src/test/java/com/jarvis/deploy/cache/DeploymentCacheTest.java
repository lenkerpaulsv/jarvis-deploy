package com.jarvis.deploy.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentCacheTest {

    private DeploymentCache cache;

    @BeforeEach
    void setUp() {
        cache = new DeploymentCache(5000L, 10);
    }

    @Test
    void putAndGet_returnsStoredValue() {
        cache.put("env:prod", "v1.2.3");
        Optional<String> result = cache.get("env:prod");
        assertTrue(result.isPresent());
        assertEquals("v1.2.3", result.get());
    }

    @Test
    void get_missingKey_returnsEmpty() {
        Optional<String> result = cache.get("nonexistent");
        assertFalse(result.isPresent());
    }

    @Test
    void invalidate_removesEntry() {
        cache.put("env:staging", "v1.1.0");
        cache.invalidate("env:staging");
        assertFalse(cache.get("env:staging").isPresent());
    }

    @Test
    void clear_emptiesAllEntries() {
        cache.put("k1", "v1");
        cache.put("k2", "v2");
        cache.clear();
        assertEquals(0, cache.size());
    }

    @Test
    void put_expiredEntry_returnsEmpty() throws InterruptedException {
        DeploymentCache shortCache = new DeploymentCache(50L, 10);
        shortCache.put("key", "value");
        Thread.sleep(100);
        assertFalse(shortCache.get("key").isPresent());
    }

    @Test
    void put_exceedsMaxSize_evictsOldestEntry() {
        DeploymentCache smallCache = new DeploymentCache(5000L, 3);
        smallCache.put("a", "1");
        smallCache.put("b", "2");
        smallCache.put("c", "3");
        smallCache.put("d", "4");
        assertEquals(3, smallCache.size());
        assertFalse(smallCache.get("a").isPresent());
    }

    @Test
    void put_blankKey_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> cache.put(" ", "value"));
    }

    @Test
    void constructor_invalidTtl_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new DeploymentCache(0L, 10));
    }

    @Test
    void constructor_invalidMaxSize_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new DeploymentCache(1000L, 0));
    }

    @Test
    void put_overwritesExistingKey() {
        cache.put("env:dev", "v1.0.0");
        cache.put("env:dev", "v2.0.0");
        assertEquals("v2.0.0", cache.get("env:dev").orElse(null));
    }
}
