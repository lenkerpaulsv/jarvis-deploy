package com.jarvis.deploy.quota;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentQuotaManagerTest {

    private DeploymentQuotaManager manager;

    @BeforeEach
    void setUp() {
        manager = new DeploymentQuotaManager(3);
    }

    @Test
    void defaultQuotaIsUsedWhenNoneSet() {
        assertEquals(3, manager.getQuota("staging"));
    }

    @Test
    void customQuotaOverridesDefault() {
        manager.setQuota("production", 5);
        assertEquals(5, manager.getQuota("production"));
    }

    @Test
    void consumeIncrementsUsage() {
        assertTrue(manager.tryConsume("staging"));
        assertEquals(1, manager.getUsage("staging"));
    }

    @Test
    void consumeFailsWhenQuotaExceeded() {
        manager.setQuota("dev", 2);
        assertTrue(manager.tryConsume("dev"));
        assertTrue(manager.tryConsume("dev"));
        assertFalse(manager.tryConsume("dev"));
        assertEquals(2, manager.getUsage("dev"));
    }

    @Test
    void isExceededReturnsTrueAtLimit() {
        manager.setQuota("qa", 1);
        manager.tryConsume("qa");
        assertTrue(manager.isExceeded("qa"));
    }

    @Test
    void resetClearsUsageForEnvironment() {
        manager.tryConsume("staging");
        manager.reset("staging");
        assertEquals(0, manager.getUsage("staging"));
    }

    @Test
    void resetAllClearsAllUsage() {
        manager.tryConsume("staging");
        manager.tryConsume("production");
        manager.resetAll();
        assertEquals(0, manager.getUsage("staging"));
        assertEquals(0, manager.getUsage("production"));
    }

    @Test
    void invalidLimitThrows() {
        assertThrows(IllegalArgumentException.class, () -> manager.setQuota("env", 0));
    }

    @Test
    void blankEnvironmentThrows() {
        assertThrows(IllegalArgumentException.class, () -> manager.setQuota(" ", 5));
    }
}
