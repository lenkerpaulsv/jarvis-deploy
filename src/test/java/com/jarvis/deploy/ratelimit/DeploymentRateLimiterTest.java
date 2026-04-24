package com.jarvis.deploy.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentRateLimiterTest {

    private DeploymentRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = new DeploymentRateLimiter(3, 60);
    }

    @Test
    void shouldAllowDeploymentWhenUnderLimit() {
        assertTrue(rateLimiter.tryAcquire("production"));
    }

    @Test
    void shouldTrackCountAfterAcquire() {
        rateLimiter.tryAcquire("staging");
        rateLimiter.tryAcquire("staging");
        assertEquals(2, rateLimiter.currentCount("staging"));
    }

    @Test
    void shouldDenyDeploymentWhenLimitReached() {
        rateLimiter.tryAcquire("production");
        rateLimiter.tryAcquire("production");
        rateLimiter.tryAcquire("production");
        assertFalse(rateLimiter.tryAcquire("production"));
    }

    @Test
    void shouldTrackEnvironmentsIndependently() {
        rateLimiter.tryAcquire("production");
        rateLimiter.tryAcquire("production");
        rateLimiter.tryAcquire("production");
        assertTrue(rateLimiter.tryAcquire("staging"));
    }

    @Test
    void shouldReturnZeroCountForUnknownEnvironment() {
        assertEquals(0, rateLimiter.currentCount("unknown-env"));
    }

    @Test
    void shouldResetCountForEnvironment() {
        rateLimiter.tryAcquire("production");
        rateLimiter.tryAcquire("production");
        rateLimiter.reset("production");
        assertEquals(0, rateLimiter.currentCount("production"));
        assertTrue(rateLimiter.tryAcquire("production"));
    }

    @Test
    void shouldExposeConfiguredLimits() {
        assertEquals(3, rateLimiter.getMaxDeployments());
        assertEquals(60, rateLimiter.getWindowSeconds());
    }

    @Test
    void shouldRejectNullEnvironment() {
        assertThrows(IllegalArgumentException.class, () -> rateLimiter.tryAcquire(null));
    }

    @Test
    void shouldRejectBlankEnvironment() {
        assertThrows(IllegalArgumentException.class, () -> rateLimiter.tryAcquire("  "));
    }

    @Test
    void shouldRejectInvalidMaxDeployments() {
        assertThrows(IllegalArgumentException.class, () -> new DeploymentRateLimiter(0, 60));
    }

    @Test
    void shouldRejectInvalidWindowSeconds() {
        assertThrows(IllegalArgumentException.class, () -> new DeploymentRateLimiter(5, -1));
    }

    @Test
    void shouldAllowDeploymentAfterResetEvenAtLimit() {
        rateLimiter.tryAcquire("dev");
        rateLimiter.tryAcquire("dev");
        rateLimiter.tryAcquire("dev");
        assertFalse(rateLimiter.tryAcquire("dev"));
        rateLimiter.reset("dev");
        assertTrue(rateLimiter.tryAcquire("dev"));
    }
}
