package com.jarvis.deploy.metrics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentMetricsTest {

    private DeploymentMetrics metrics;

    @BeforeEach
    void setUp() {
        metrics = new DeploymentMetrics("staging");
    }

    @Test
    void constructor_nullEnvironment_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> new DeploymentMetrics(null));
    }

    @Test
    void constructor_blankEnvironment_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> new DeploymentMetrics("  "));
    }

    @Test
    void initialState_allCountsAreZero() {
        assertEquals(0, metrics.getTotalDeployments());
        assertEquals(0, metrics.getSuccessfulDeployments());
        assertEquals(0, metrics.getFailedDeployments());
        assertEquals(0, metrics.getRollbacks());
        assertEquals(0.0, metrics.getSuccessRate());
        assertEquals(0L, metrics.getAverageDeploymentMillis());
    }

    @Test
    void recordDeployment_success_incrementsCorrectly() {
        Instant start = Instant.now();
        Instant end = start.plusMillis(500);
        metrics.recordDeployment(true, start, end);

        assertEquals(1, metrics.getTotalDeployments());
        assertEquals(1, metrics.getSuccessfulDeployments());
        assertEquals(0, metrics.getFailedDeployments());
        assertEquals(100.0, metrics.getSuccessRate(), 0.01);
        assertEquals(500L, metrics.getAverageDeploymentMillis());
    }

    @Test
    void recordDeployment_failure_incrementsCorrectly() {
        Instant start = Instant.now();
        Instant end = start.plusMillis(200);
        metrics.recordDeployment(false, start, end);

        assertEquals(1, metrics.getTotalDeployments());
        assertEquals(0, metrics.getSuccessfulDeployments());
        assertEquals(1, metrics.getFailedDeployments());
        assertEquals(0.0, metrics.getSuccessRate(), 0.01);
    }

    @Test
    void recordRollback_incrementsRollbackCount() {
        metrics.recordRollback();
        metrics.recordRollback();
        assertEquals(2, metrics.getRollbacks());
    }

    @Test
    void getSuccessRate_mixedResults_calculatesCorrectly() {
        Instant now = Instant.now();
        metrics.recordDeployment(true, now, now.plusMillis(100));
        metrics.recordDeployment(true, now, now.plusMillis(100));
        metrics.recordDeployment(false, now, now.plusMillis(100));

        assertEquals(66.67, metrics.getSuccessRate(), 0.1);
    }

    @Test
    void getEnvironment_returnsCorrectValue() {
        assertEquals("staging", metrics.getEnvironment());
    }
}
