package com.jarvis.deploy.status;

import com.jarvis.deploy.deployment.DeploymentHistory;
import com.jarvis.deploy.deployment.DeploymentRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeploymentStatusTest {

    private DeploymentHistory history;
    private DeploymentStatus status;

    @BeforeEach
    void setUp() {
        history = mock(DeploymentHistory.class);
        status = new DeploymentStatus(history);
    }

    @Test
    void constructorThrowsOnNullHistory() {
        assertThrows(IllegalArgumentException.class, () -> new DeploymentStatus(null));
    }

    @Test
    void getLatestReturnsEmptyWhenNoRecords() {
        when(history.getRecordsForEnvironment("staging")).thenReturn(java.util.Collections.emptyList());
        Optional<DeploymentRecord> result = status.getLatest("staging");
        assertTrue(result.isEmpty());
    }

    @Test
    void getLatestReturnsLastRecord() {
        DeploymentRecord r1 = mock(DeploymentRecord.class);
        DeploymentRecord r2 = mock(DeploymentRecord.class);
        when(history.getRecordsForEnvironment("prod")).thenReturn(java.util.List.of(r1, r2));
        Optional<DeploymentRecord> result = status.getLatest("prod");
        assertTrue(result.isPresent());
        assertSame(r2, result.get());
    }

    @Test
    void getSummaryReturnsNoDeploymentMessageWhenEmpty() {
        when(history.getRecordsForEnvironment("dev")).thenReturn(java.util.Collections.emptyList());
        String summary = status.getSummary("dev");
        assertTrue(summary.contains("No deployments recorded"));
    }

    @Test
    void getSummaryContainsVersionAndStatus() {
        DeploymentRecord record = mock(DeploymentRecord.class);
        when(record.getVersion()).thenReturn("1.2.3");
        when(record.getStatus()).thenReturn("SUCCESS");
        when(record.getTimestamp()).thenReturn(Instant.now().toString());
        when(history.getRecordsForEnvironment("prod")).thenReturn(java.util.List.of(record));
        String summary = status.getSummary("prod");
        assertTrue(summary.contains("1.2.3"));
        assertTrue(summary.contains("SUCCESS"));
    }

    @Test
    void isHealthyReturnsTrueForSuccessStatus() {
        DeploymentRecord record = mock(DeploymentRecord.class);
        when(record.getStatus()).thenReturn("SUCCESS");
        when(history.getRecordsForEnvironment("staging")).thenReturn(java.util.List.of(record));
        assertTrue(status.isHealthy("staging"));
    }

    @Test
    void isHealthyReturnsFalseForFailedStatus() {
        DeploymentRecord record = mock(DeploymentRecord.class);
        when(record.getStatus()).thenReturn("FAILED");
        when(history.getRecordsForEnvironment("staging")).thenReturn(java.util.List.of(record));
        assertFalse(status.isHealthy("staging"));
    }

    @Test
    void isHealthyReturnsFalseWhenNoRecords() {
        when(history.getRecordsForEnvironment("prod")).thenReturn(java.util.Collections.emptyList());
        assertFalse(status.isHealthy("prod"));
    }

    @Test
    void getLatestThrowsOnBlankEnvironment() {
        assertThrows(IllegalArgumentException.class, () -> status.getLatest(""));
        assertThrows(IllegalArgumentException.class, () -> status.getLatest(null));
    }
}
