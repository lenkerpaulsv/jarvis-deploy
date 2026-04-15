package com.jarvis.deploy.health;

import com.jarvis.deploy.config.EnvironmentConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HealthCheckerTest {

    private HealthChecker healthChecker;
    private EnvironmentConfig config;

    @BeforeEach
    void setUp() {
        healthChecker = new HealthChecker(2000);
        config = mock(EnvironmentConfig.class);
    }

    @Test
    void check_nullConfig_returnsFailure() {
        HealthCheckResult result = healthChecker.check(null);
        assertFalse(result.isHealthy());
        assertTrue(result.getMessage().contains("null"));
    }

    @Test
    void check_noHealthCheckUrl_skipEndpointCheck_validDir_returnsSuccess() {
        when(config.getEnvironmentName()).thenReturn("staging");
        when(config.getHealthCheckUrl()).thenReturn(null);
        when(config.getDeployDirectory()).thenReturn(System.getProperty("java.io.tmpdir"));

        HealthCheckResult result = healthChecker.check(config);

        assertTrue(result.isHealthy(), "Expected healthy result but got: " + result.getMessage());
    }

    @Test
    void check_blankHealthCheckUrl_skipEndpointCheck_validDir_returnsSuccess() {
        when(config.getEnvironmentName()).thenReturn("production");
        when(config.getHealthCheckUrl()).thenReturn("   ");
        when(config.getDeployDirectory()).thenReturn(System.getProperty("java.io.tmpdir"));

        HealthCheckResult result = healthChecker.check(config);

        assertTrue(result.isHealthy());
    }

    @Test
    void check_unreachableEndpoint_returnsFailure() {
        when(config.getEnvironmentName()).thenReturn("dev");
        when(config.getHealthCheckUrl()).thenReturn("http://localhost:19999/health");
        when(config.getDeployDirectory()).thenReturn(System.getProperty("java.io.tmpdir"));

        HealthCheckResult result = healthChecker.check(config);

        assertFalse(result.isHealthy());
        assertTrue(result.getMessage().contains("unreachable") || result.getMessage().contains("localhost:19999"));
    }

    @Test
    void check_missingDeployDirectory_nullPath_returnsFailure() {
        when(config.getEnvironmentName()).thenReturn("dev");
        when(config.getHealthCheckUrl()).thenReturn(null);
        when(config.getDeployDirectory()).thenReturn(null);

        HealthCheckResult result = healthChecker.check(config);

        assertFalse(result.isHealthy());
        assertTrue(result.getMessage().toLowerCase().contains("deploy directory"));
    }

    @Test
    void check_blankDeployDirectory_returnsFailure() {
        when(config.getEnvironmentName()).thenReturn("dev");
        when(config.getHealthCheckUrl()).thenReturn("");
        when(config.getDeployDirectory()).thenReturn("");

        HealthCheckResult result = healthChecker.check(config);

        assertFalse(result.isHealthy());
    }
}
