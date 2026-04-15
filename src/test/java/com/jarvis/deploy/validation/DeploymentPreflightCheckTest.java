package com.jarvis.deploy.validation;

import com.jarvis.deploy.config.EnvironmentConfig;
import com.jarvis.deploy.deployment.DeploymentRecord;
import com.jarvis.deploy.health.HealthChecker;
import com.jarvis.deploy.lock.DeploymentLockManager;
import com.jarvis.deploy.validation.DeploymentPreflightCheck.PreflightResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeploymentPreflightCheckTest {

    private HealthChecker healthChecker;
    private DeploymentLockManager lockManager;
    private DeploymentPreflightCheck preflightCheck;

    private EnvironmentConfig config;
    private DeploymentRecord record;

    @BeforeEach
    void setUp() {
        healthChecker = mock(HealthChecker.class);
        lockManager = mock(DeploymentLockManager.class);
        preflightCheck = new DeploymentPreflightCheck(healthChecker, lockManager);

        config = mock(EnvironmentConfig.class);
        when(config.getEnvironmentName()).thenReturn("staging");

        record = mock(DeploymentRecord.class);
        when(record.getArtifactPath()).thenReturn("/releases/app-1.2.3.jar");
        when(record.getVersion()).thenReturn("1.2.3");
    }

    @Test
    void shouldPassWhenAllChecksSucceed() {
        when(healthChecker.isHealthy("staging")).thenReturn(true);
        when(lockManager.isLocked("staging")).thenReturn(false);

        PreflightResult result = preflightCheck.run(config, record);

        assertTrue(result.isPassed());
        assertTrue(result.getViolations().isEmpty());
    }

    @Test
    void shouldFailWhenEnvironmentIsUnhealthy() {
        when(healthChecker.isHealthy("staging")).thenReturn(false);
        when(lockManager.isLocked("staging")).thenReturn(false);

        PreflightResult result = preflightCheck.run(config, record);

        assertFalse(result.isPassed());
        assertEquals(1, result.getViolations().size());
        assertTrue(result.getViolations().get(0).contains("health check"));
    }

    @Test
    void shouldFailWhenEnvironmentIsLocked() {
        when(healthChecker.isHealthy("staging")).thenReturn(true);
        when(lockManager.isLocked("staging")).thenReturn(true);

        PreflightResult result = preflightCheck.run(config, record);

        assertFalse(result.isPassed());
        assertEquals(1, result.getViolations().size());
        assertTrue(result.getViolations().get(0).contains("locked"));
    }

    @Test
    void shouldFailWhenArtifactPathIsBlank() {
        when(healthChecker.isHealthy("staging")).thenReturn(true);
        when(lockManager.isLocked("staging")).thenReturn(false);
        when(record.getArtifactPath()).thenReturn("  ");

        PreflightResult result = preflightCheck.run(config, record);

        assertFalse(result.isPassed());
        assertTrue(result.getViolations().stream().anyMatch(v -> v.contains("artifact path")));
    }

    @Test
    void shouldFailWhenVersionIsNull() {
        when(healthChecker.isHealthy("staging")).thenReturn(true);
        when(lockManager.isLocked("staging")).thenReturn(false);
        when(record.getVersion()).thenReturn(null);

        PreflightResult result = preflightCheck.run(config, record);

        assertFalse(result.isPassed());
        assertTrue(result.getViolations().stream().anyMatch(v -> v.contains("version")));
    }

    @Test
    void shouldAccumulateMultipleViolations() {
        when(healthChecker.isHealthy("staging")).thenReturn(false);
        when(lockManager.isLocked("staging")).thenReturn(true);
        when(record.getArtifactPath()).thenReturn("");
        when(record.getVersion()).thenReturn("");

        PreflightResult result = preflightCheck.run(config, record);

        assertFalse(result.isPassed());
        assertEquals(4, result.getViolations().size());
    }

    @Test
    void shouldThrowWhenConfigIsNull() {
        assertThrows(IllegalArgumentException.class, () -> preflightCheck.run(null, record));
    }

    @Test
    void shouldThrowWhenRecordIsNull() {
        assertThrows(IllegalArgumentException.class, () -> preflightCheck.run(config, null));
    }
}
