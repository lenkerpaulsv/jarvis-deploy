package com.jarvis.deploy.metrics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class MetricsReporterTest {

    private MetricsReporter reporter;

    @BeforeEach
    void setUp() {
        reporter = new MetricsReporter();
    }

    @Test
    void getOrCreate_newEnvironment_returnsMetrics() {
        DeploymentMetrics metrics = reporter.getOrCreate("production");
        assertNotNull(metrics);
        assertEquals("production", metrics.getEnvironment());
    }

    @Test
    void getOrCreate_sameEnvironment_returnsSameInstance() {
        DeploymentMetrics first = reporter.getOrCreate("staging");
        DeploymentMetrics second = reporter.getOrCreate("staging");
        assertSame(first, second);
    }

    @Test
    void snapshot_unknownEnvironment_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> reporter.snapshot("unknown"));
    }

    @Test
    void snapshot_afterRecording_capturesCorrectData() {
        DeploymentMetrics metrics = reporter.getOrCreate("dev");
        Instant now = Instant.now();
        metrics.recordDeployment(true, now, now.plusMillis(300));
        metrics.recordRollback();

        MetricsSnapshot snap = reporter.snapshot("dev");

        assertEquals("dev", snap.getEnvironment());
        assertEquals(1, snap.getTotalDeployments());
        assertEquals(1, snap.getSuccessfulDeployments());
        assertEquals(0, snap.getFailedDeployments());
        assertEquals(1, snap.getRollbacks());
        assertEquals(100.0, snap.getSuccessRate(), 0.01);
        assertEquals(300L, snap.getAverageDeploymentMillis());
        assertNotNull(snap.getCapturedAt());
    }

    @Test
    void hasMetrics_beforeAndAfterCreate() {
        assertFalse(reporter.hasMetrics("qa"));
        reporter.getOrCreate("qa");
        assertTrue(reporter.hasMetrics("qa"));
    }

    @Test
    void reset_removesEnvironmentMetrics() {
        reporter.getOrCreate("staging");
        assertTrue(reporter.hasMetrics("staging"));
        reporter.reset("staging");
        assertFalse(reporter.hasMetrics("staging"));
    }

    @Test
    void printReport_doesNotThrow() {
        DeploymentMetrics metrics = reporter.getOrCreate("production");
        Instant now = Instant.now();
        metrics.recordDeployment(true, now, now.plusMillis(1000));
        assertDoesNotThrow(() -> reporter.printReport("production"));
    }
}
