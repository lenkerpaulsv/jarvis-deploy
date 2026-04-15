package com.jarvis.deploy.lock;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentLockTest {

    @Test
    void testLockFieldsAreStoredCorrectly() {
        Instant now = Instant.now();
        DeploymentLock lock = new DeploymentLock("lock-1", "production", "alice", now);

        assertEquals("lock-1", lock.getLockId());
        assertEquals("production", lock.getEnvironment());
        assertEquals("alice", lock.getOwner());
        assertEquals(now, lock.getAcquiredAt());
    }

    @Test
    void testIsNotExpiredWhenRecentlyAcquired() {
        DeploymentLock lock = new DeploymentLock("lock-2", "staging", "bob", Instant.now());
        assertFalse(lock.isExpired(600));
    }

    @Test
    void testIsExpiredWhenAcquiredInThePast() {
        Instant pastTime = Instant.now().minusSeconds(700);
        DeploymentLock lock = new DeploymentLock("lock-3", "staging", "bob", pastTime);
        assertTrue(lock.isExpired(600));
    }

    @Test
    void testToStringContainsKeyInfo() {
        DeploymentLock lock = new DeploymentLock("lock-4", "dev", "carol", Instant.now());
        String str = lock.toString();
        assertTrue(str.contains("lock-4"));
        assertTrue(str.contains("dev"));
        assertTrue(str.contains("carol"));
    }
}
