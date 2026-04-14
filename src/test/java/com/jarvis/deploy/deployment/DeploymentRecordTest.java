package com.jarvis.deploy.deployment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentRecordTest {

    private DeploymentRecord record;

    @BeforeEach
    void setUp() {
        record = new DeploymentRecord("1.0.0", "staging", "/artifacts/app-1.0.0.jar");
    }

    @Test
    void constructor_setsFieldsCorrectly() {
        assertEquals("1.0.0", record.getVersion());
        assertEquals("staging", record.getEnvironment());
        assertEquals("/artifacts/app-1.0.0.jar", record.getArtifactPath());
    }

    @Test
    void constructor_setsInitialStatusToInProgress() {
        assertEquals(DeploymentRecord.DeploymentStatus.IN_PROGRESS, record.getStatus());
    }

    @Test
    void constructor_setsDeployedAtToNow() {
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);
        assertTrue(record.getDeployedAt().isAfter(before));
        assertTrue(record.getDeployedAt().isBefore(after));
    }

    @Test
    void setStatus_updatesStatus() {
        record.setStatus(DeploymentRecord.DeploymentStatus.SUCCESS);
        assertEquals(DeploymentRecord.DeploymentStatus.SUCCESS, record.getStatus());
    }

    @Test
    void setStatus_toFailed() {
        record.setStatus(DeploymentRecord.DeploymentStatus.FAILED);
        assertEquals(DeploymentRecord.DeploymentStatus.FAILED, record.getStatus());
    }

    @Test
    void setStatus_toRolledBack() {
        record.setStatus(DeploymentRecord.DeploymentStatus.ROLLED_BACK);
        assertEquals(DeploymentRecord.DeploymentStatus.ROLLED_BACK, record.getStatus());
    }

    @Test
    void toString_containsKeyFields() {
        String result = record.toString();
        assertTrue(result.contains("1.0.0"));
        assertTrue(result.contains("staging"));
        assertTrue(result.contains("IN_PROGRESS"));
    }
}
