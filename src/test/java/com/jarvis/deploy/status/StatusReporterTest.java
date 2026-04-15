package com.jarvis.deploy.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StatusReporterTest {

    private DeploymentStatus deploymentStatus;
    private ByteArrayOutputStream outputStream;
    private StatusReporter reporter;

    @BeforeEach
    void setUp() {
        deploymentStatus = mock(DeploymentStatus.class);
        outputStream = new ByteArrayOutputStream();
        reporter = new StatusReporter(deploymentStatus, new PrintStream(outputStream));
    }

    @Test
    void constructorThrowsOnNullStatus() {
        assertThrows(IllegalArgumentException.class,
                () -> new StatusReporter(null, System.out));
    }

    @Test
    void constructorThrowsOnNullPrintStream() {
        assertThrows(IllegalArgumentException.class,
                () -> new StatusReporter(deploymentStatus, null));
    }

    @Test
    void reportPrintsSummaryAndHealth() {
        when(deploymentStatus.getSummary("prod")).thenReturn("[prod] Latest: version=1.0, status=SUCCESS, timestamp=...");
        when(deploymentStatus.isHealthy("prod")).thenReturn(true);
        reporter.report("prod");
        String output = outputStream.toString();
        assertTrue(output.contains("[prod]"));
        assertTrue(output.contains("Health: OK"));
    }

    @Test
    void reportPrintsDegradedWhenUnhealthy() {
        when(deploymentStatus.getSummary("staging")).thenReturn("[staging] Latest: version=0.9, status=FAILED, timestamp=...");
        when(deploymentStatus.isHealthy("staging")).thenReturn(false);
        reporter.report("staging");
        String output = outputStream.toString();
        assertTrue(output.contains("Health: DEGRADED"));
    }

    @Test
    void reportAllPrintsHeaderAndAllEnvironments() {
        when(deploymentStatus.getSummary(anyString())).thenReturn("summary");
        when(deploymentStatus.isHealthy(anyString())).thenReturn(true);
        reporter.reportAll(List.of("dev", "staging", "prod"));
        String output = outputStream.toString();
        assertTrue(output.contains("Deployment Status Report"));
        verify(deploymentStatus, times(3)).getSummary(anyString());
    }

    @Test
    void reportAllHandlesEmptyList() {
        reporter.reportAll(List.of());
        String output = outputStream.toString();
        assertTrue(output.contains("No environments configured"));
    }

    @Test
    void healthCheckPrintsOkForHealthyEnv() {
        when(deploymentStatus.isHealthy("prod")).thenReturn(true);
        reporter.healthCheck(List.of("prod"));
        String output = outputStream.toString();
        assertTrue(output.contains("[OK]"));
    }

    @Test
    void healthCheckPrintsDegradedForUnhealthyEnv() {
        when(deploymentStatus.isHealthy("dev")).thenReturn(false);
        reporter.healthCheck(List.of("dev"));
        String output = outputStream.toString();
        assertTrue(output.contains("[DEGRADED]"));
    }

    @Test
    void healthCheckHandlesNullList() {
        reporter.healthCheck(null);
        String output = outputStream.toString();
        assertTrue(output.contains("No environments to check"));
    }
}
