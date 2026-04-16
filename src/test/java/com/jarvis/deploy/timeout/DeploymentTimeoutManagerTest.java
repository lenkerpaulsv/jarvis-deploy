package com.jarvis.deploy.timeout;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentTimeoutManagerTest {

    private DeploymentTimeoutManager manager;

    @BeforeEach
    void setUp() {
        manager = new DeploymentTimeoutManager();
    }

    @Test
    void shouldReturnDefaultTimeoutForUnknownEnvironment() {
        assertEquals(DeploymentTimeoutManager.DEFAULT_TIMEOUT, manager.getTimeout("unknown"));
    }

    @Test
    void shouldConfigureAndRetrieveCustomTimeout() {
        manager.configureTimeout("production", Duration.ofMinutes(45));
        assertEquals(Duration.ofMinutes(45), manager.getTimeout("production"));
    }

    @Test
    void shouldRejectBlankEnvironmentInConfigure() {
        assertThrows(IllegalArgumentException.class, () -> manager.configureTimeout("", Duration.ofMinutes(10)));
    }

    @Test
    void shouldRejectNonPositiveTimeout() {
        assertThrows(IllegalArgumentException.class, () -> manager.configureTimeout("staging", Duration.ZERO));
        assertThrows(IllegalArgumentException.class, () -> manager.configureTimeout("staging", Duration.ofMinutes(-1)));
    }

    @Test
    void shouldTrackDeploymentStart() {
        manager.startTracking("deploy-001");
        assertTrue(manager.isTracking("deploy-001"));
    }

    @Test
    void shouldReturnElapsedTimeAfterStart() throws InterruptedException {
        manager.startTracking("deploy-002");
        Thread.sleep(50);
        Duration elapsed = manager.elapsed("deploy-002");
        assertTrue(elapsed.toMillis() >= 50);
    }

    @Test
    void shouldNotBeTimedOutImmediately() {
        manager.configureTimeout("dev", Duration.ofMinutes(5));
        manager.startTracking("deploy-003");
        assertFalse(manager.isTimedOut("deploy-003", "dev"));
    }

    @Test
    void shouldDetectTimeoutWithVeryShortDuration() throws InterruptedException {
        manager.configureTimeout("ci", Duration.ofMillis(50));
        manager.startTracking("deploy-004");
        Thread.sleep(100);
        assertTrue(manager.isTimedOut("deploy-004", "ci"));
    }

    @Test
    void shouldStopTracking() {
        manager.startTracking("deploy-005");
        manager.stopTracking("deploy-005");
        assertFalse(manager.isTracking("deploy-005"));
    }

    @Test
    void shouldThrowWhenElapsedCalledWithoutTracking() {
        assertThrows(IllegalStateException.class, () -> manager.elapsed("not-tracked"));
    }

    @Test
    void shouldThrowWhenIsTimedOutCalledWithoutTracking() {
        assertThrows(IllegalStateException.class, () -> manager.isTimedOut("not-tracked", "prod"));
    }
}
