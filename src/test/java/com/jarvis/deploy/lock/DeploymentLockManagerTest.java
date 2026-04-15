package com.jarvis.deploy.lock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentLockManagerTest {

    private DeploymentLockManager lockManager;

    @BeforeEach
    void setUp() {
        lockManager = new DeploymentLockManager(600);
    }

    @Test
    void testAcquireLockSuccessfully() {
        Optional<DeploymentLock> lock = lockManager.acquireLock("production", "alice");
        assertTrue(lock.isPresent());
        assertEquals("production", lock.get().getEnvironment());
        assertEquals("alice", lock.get().getOwner());
    }

    @Test
    void testAcquireLockFailsWhenAlreadyLocked() {
        lockManager.acquireLock("production", "alice");
        Optional<DeploymentLock> second = lockManager.acquireLock("production", "bob");
        assertFalse(second.isPresent());
    }

    @Test
    void testAcquireLockSucceedsAfterExpiry() {
        DeploymentLockManager shortTtlManager = new DeploymentLockManager(0);
        shortTtlManager.acquireLock("staging", "alice");
        Optional<DeploymentLock> second = shortTtlManager.acquireLock("staging", "bob");
        assertTrue(second.isPresent());
    }

    @Test
    void testReleaseLockSuccessfully() {
        Optional<DeploymentLock> lock = lockManager.acquireLock("staging", "alice");
        assertTrue(lock.isPresent());
        boolean released = lockManager.releaseLock("staging", lock.get().getLockId());
        assertTrue(released);
        assertFalse(lockManager.isLocked("staging"));
    }

    @Test
    void testReleaseLockFailsWithWrongId() {
        lockManager.acquireLock("staging", "alice");
        boolean released = lockManager.releaseLock("staging", "wrong-id");
        assertFalse(released);
        assertTrue(lockManager.isLocked("staging"));
    }

    @Test
    void testIsLockedReturnsFalseForUnlockedEnvironment() {
        assertFalse(lockManager.isLocked("dev"));
    }

    @Test
    void testGetCurrentLockReturnsEmptyWhenNotLocked() {
        Optional<DeploymentLock> lock = lockManager.getCurrentLock("dev");
        assertFalse(lock.isPresent());
    }

    @Test
    void testAcquireLockThrowsOnBlankEnvironment() {
        assertThrows(IllegalArgumentException.class, () -> lockManager.acquireLock("", "alice"));
    }

    @Test
    void testAcquireLockThrowsOnBlankOwner() {
        assertThrows(IllegalArgumentException.class, () -> lockManager.acquireLock("production", ""));
    }

    @Test
    void testIndependentLocksPerEnvironment() {
        Optional<DeploymentLock> lock1 = lockManager.acquireLock("production", "alice");
        Optional<DeploymentLock> lock2 = lockManager.acquireLock("staging", "bob");
        assertTrue(lock1.isPresent());
        assertTrue(lock2.isPresent());
    }
}
