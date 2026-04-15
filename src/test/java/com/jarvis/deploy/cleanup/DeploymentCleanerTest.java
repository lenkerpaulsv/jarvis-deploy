package com.jarvis.deploy.cleanup;

import com.jarvis.deploy.audit.AuditLogger;
import com.jarvis.deploy.deployment.DeploymentHistory;
import com.jarvis.deploy.deployment.DeploymentRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeploymentCleanerTest {

    @Mock
    private DeploymentHistory deploymentHistory;

    @Mock
    private AuditLogger auditLogger;

    private DeploymentCleaner cleaner;

    @BeforeEach
    void setUp() {
        cleaner = new DeploymentCleaner(deploymentHistory, auditLogger, 30, "/tmp/artifacts");
    }

    @Test
    void constructor_invalidRetentionDays_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> new DeploymentCleaner(deploymentHistory, auditLogger, 0, "/tmp"));
        assertThrows(IllegalArgumentException.class,
                () -> new DeploymentCleaner(deploymentHistory, auditLogger, -5, "/tmp"));
    }

    @Test
    void getRetentionDays_returnsConfiguredValue() {
        assertEquals(30, cleaner.getRetentionDays());
    }

    @Test
    void cleanExpiredRecords_removesOnlyExpiredRecords() {
        DeploymentRecord old = mock(DeploymentRecord.class);
        DeploymentRecord recent = mock(DeploymentRecord.class);

        when(old.getTimestamp()).thenReturn(Instant.now().minus(60, ChronoUnit.DAYS));
        when(old.getDeploymentId()).thenReturn("deploy-old-001");
        when(recent.getTimestamp()).thenReturn(Instant.now().minus(5, ChronoUnit.DAYS));

        List<DeploymentRecord> history = Arrays.asList(old, recent);
        when(deploymentHistory.getHistory("staging")).thenReturn(history);

        CleanupResult result = cleaner.cleanExpiredRecords("staging");

        assertEquals(1, result.getRecordsRemoved());
        verify(deploymentHistory).removeRecord("staging", "deploy-old-001");
        verify(deploymentHistory, never()).removeRecord(eq("staging"), argThat(id -> !id.equals("deploy-old-001")));
        verify(auditLogger).log(eq("CLEANUP"), eq("staging"), anyString());
    }

    @Test
    void cleanExpiredRecords_noExpiredRecords_returnsZero() {
        DeploymentRecord recent = mock(DeploymentRecord.class);
        when(recent.getTimestamp()).thenReturn(Instant.now().minus(2, ChronoUnit.DAYS));
        when(deploymentHistory.getHistory("production")).thenReturn(Collections.singletonList(recent));

        CleanupResult result = cleaner.cleanExpiredRecords("production");

        assertEquals(0, result.getRecordsRemoved());
        verify(deploymentHistory, never()).removeRecord(anyString(), anyString());
    }

    @Test
    void cleanExpiredRecords_emptyHistory_returnsZero() {
        when(deploymentHistory.getHistory("dev")).thenReturn(Collections.emptyList());

        CleanupResult result = cleaner.cleanExpiredRecords("dev");

        assertEquals(0, result.getRecordsRemoved());
        assertEquals(0, result.getArtifactsDeleted());
    }

    @Test
    void cleanArtifacts_nonExistentDirectory_returnsZeroDeleted() {
        DeploymentCleaner cleanerWithBadPath = new DeploymentCleaner(
                deploymentHistory, auditLogger, 30, "/nonexistent/path/xyz");

        CleanupResult result = cleanerWithBadPath.cleanArtifacts("staging");

        assertEquals(0, result.getArtifactsDeleted());
    }
}
