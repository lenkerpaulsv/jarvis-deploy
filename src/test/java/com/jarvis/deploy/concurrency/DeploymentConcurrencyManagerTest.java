package com.jarvis.deploy.concurrency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentConcurrencyManagerTest {

    private DeploymentConcurrencyManager manager;

    @BeforeEach
    void setUp() {
        manager = new DeploymentConcurrencyManager();
    }

    @Test
    void testAcquireWithinLimit() {
        manager.registerPolicy(new ConcurrencyPolicy("staging", 2, true, 5000));
        assertDoesNotThrow(() -> manager.acquire("staging", "dep-1", "app-a"));
        assertDoesNotThrow(() -> manager.acquire("staging", "dep-2", "app-b"));
        assertEquals(2, manager.getActiveCount("staging"));
    }

    @Test
    void testAcquireExceedsLimit() {
        manager.registerPolicy(new ConcurrencyPolicy("staging", 1, true, 5000));
        manager.acquire("staging", "dep-1", "app-a");
        ConcurrencyViolationException ex = assertThrows(
                ConcurrencyViolationException.class,
                () -> manager.acquire("staging", "dep-2", "app-b")
        );
        assertEquals("staging", ex.getEnvironment());
        assertEquals(1, ex.getCurrentCount());
        assertEquals(1, ex.getMaxAllowed());
    }

    @Test
    void testSameAppConcurrencyBlocked() {
        manager.registerPolicy(new ConcurrencyPolicy("prod", 5, false, 3000));
        manager.acquire("prod", "dep-1", "app-x");
        assertThrows(ConcurrencyViolationException.class,
                () -> manager.acquire("prod", "dep-2", "app-x"));
    }

    @Test
    void testSameAppConcurrencyAllowed() {
        manager.registerPolicy(new ConcurrencyPolicy("dev", 5, true, 1000));
        manager.acquire("dev", "dep-1", "app-x");
        assertDoesNotThrow(() -> manager.acquire("dev", "dep-2", "app-x"));
    }

    @Test
    void testReleaseFreesSlot() {
        manager.registerPolicy(new ConcurrencyPolicy("staging", 1, true, 5000));
        manager.acquire("staging", "dep-1", "app-a");
        manager.release("staging", "dep-1", "app-a");
        assertEquals(0, manager.getActiveCount("staging"));
        assertDoesNotThrow(() -> manager.acquire("staging", "dep-2", "app-b"));
    }

    @Test
    void testNoPolicyAllowsAll() {
        assertDoesNotThrow(() -> manager.acquire("unregistered", "dep-1", "app-a"));
        assertDoesNotThrow(() -> manager.acquire("unregistered", "dep-2", "app-b"));
        assertEquals(2, manager.getActiveCount("unregistered"));
    }

    @Test
    void testHasPolicy() {
        assertFalse(manager.hasPolicy("prod"));
        manager.registerPolicy(new ConcurrencyPolicy("prod", 3, false, 2000));
        assertTrue(manager.hasPolicy("prod"));
    }

    @Test
    void testGetPolicy() {
        ConcurrencyPolicy policy = new ConcurrencyPolicy("prod", 3, false, 2000);
        manager.registerPolicy(policy);
        ConcurrencyPolicy retrieved = manager.getPolicy("prod");
        assertNotNull(retrieved);
        assertEquals(3, retrieved.getMaxConcurrentDeployments());
        assertFalse(retrieved.isAllowSameAppConcurrency());
    }

    @Test
    void testInvalidPolicyThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new ConcurrencyPolicy("", 1, true, 1000));
        assertThrows(IllegalArgumentException.class,
                () -> new ConcurrencyPolicy("env", 0, true, 1000));
        assertThrows(IllegalArgumentException.class,
                () -> new ConcurrencyPolicy("env", 1, true, -1));
    }
}
