package com.jarvis.deploy.throttle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentThrottlerTest {

    private DeploymentThrottler throttler;

    @BeforeEach
    void setUp() {
        // 3 deployments allowed per 1-second window
        throttler = new DeploymentThrottler(3, 1000);
    }

    @Test
    void allowsDeploymentsUpToLimit() {
        assertTrue(throttler.tryAcquire("staging"));
        assertTrue(throttler.tryAcquire("staging"));
        assertTrue(throttler.tryAcquire("staging"));
    }

    @Test
    void blocksDeploymentExceedingLimit() {
        throttler.tryAcquire("prod");
        throttler.tryAcquire("prod");
        throttler.tryAcquire("prod");
        assertFalse(throttler.tryAcquire("prod"));
    }

    @Test
    void environmentsAreIsolated() {
        throttler.tryAcquire("prod");
        throttler.tryAcquire("prod");
        throttler.tryAcquire("prod");
        // staging should still be allowed
        assertTrue(throttler.tryAcquire("staging"));
    }

    @Test
    void currentCountReflectsActiveDeployments() {
        throttler.tryAcquire("dev");
        throttler.tryAcquire("dev");
        assertEquals(2, throttler.currentCount("dev"));
    }

    @Test
    void currentCountIsZeroForUnknownEnvironment() {
        assertEquals(0, throttler.currentCount("unknown"));
    }

    @Test
    void resetClearsStateForEnvironment() {
        throttler.tryAcquire("staging");
        throttler.tryAcquire("staging");
        throttler.tryAcquire("staging");
        throttler.reset("staging");
        assertEquals(0, throttler.currentCount("staging"));
        assertTrue(throttler.tryAcquire("staging"));
    }

    @Test
    void slidingWindowEvictsExpiredEntries() throws InterruptedException {
        DeploymentThrottler shortWindowThrottler = new DeploymentThrottler(2, 200);
        shortWindowThrottler.tryAcquire("qa");
        shortWindowThrottler.tryAcquire("qa");
        assertFalse(shortWindowThrottler.tryAcquire("qa"));
        // Wait for the window to expire
        Thread.sleep(250);
        assertTrue(shortWindowThrottler.tryAcquire("qa"));
    }

    @Test
    void throwsOnInvalidEnvironment() {
        assertThrows(IllegalArgumentException.class, () -> throttler.tryAcquire(null));
        assertThrows(IllegalArgumentException.class, () -> throttler.tryAcquire("  "));
    }

    @Test
    void throwsOnInvalidConstructorArgs() {
        assertThrows(IllegalArgumentException.class, () -> new DeploymentThrottler(0, 1000));
        assertThrows(IllegalArgumentException.class, () -> new DeploymentThrottler(3, 0));
    }
}
