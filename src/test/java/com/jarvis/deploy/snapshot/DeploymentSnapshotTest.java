package com.jarvis.deploy.snapshot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentSnapshotTest {

    private Map<String, String> baseConfig;
    private Instant now;

    @BeforeEach
    void setUp() {
        now = Instant.now();
        baseConfig = new HashMap<>();
        baseConfig.put("db.url", "jdbc:postgresql://prod-db:5432/app");
        baseConfig.put("app.port", "8080");
        baseConfig.put("log.level", "INFO");
    }

    @Test
    void shouldCreateSnapshotWithAllFields() {
        DeploymentSnapshot snapshot = new DeploymentSnapshot(
                "snap-001", "production", "1.4.2", now, baseConfig, "alice");

        assertEquals("snap-001", snapshot.getSnapshotId());
        assertEquals("production", snapshot.getEnvironment());
        assertEquals("1.4.2", snapshot.getArtifactVersion());
        assertEquals(now, snapshot.getCapturedAt());
        assertEquals("alice", snapshot.getDeployedBy());
        assertEquals(baseConfig, snapshot.getConfigProperties());
    }

    @Test
    void configPropertiesShouldBeImmutable() {
        DeploymentSnapshot snapshot = new DeploymentSnapshot(
                "snap-002", "staging", "1.3.0", now, baseConfig, "bob");

        assertThrows(UnsupportedOperationException.class,
                () -> snapshot.getConfigProperties().put("new.key", "value"));
    }

    @Test
    void diffConfigShouldDetectChangedValues() {
        Map<String, String> updatedConfig = new HashMap<>(baseConfig);
        updatedConfig.put("app.port", "9090");

        DeploymentSnapshot snap1 = new DeploymentSnapshot(
                "snap-001", "production", "1.4.2", now, baseConfig, "alice");
        DeploymentSnapshot snap2 = new DeploymentSnapshot(
                "snap-002", "production", "1.4.3", now, updatedConfig, "alice");

        Map<String, String[]> diff = snap1.diffConfig(snap2);

        assertTrue(diff.containsKey("app.port"));
        assertEquals("8080", diff.get("app.port")[0]);
        assertEquals("9090", diff.get("app.port")[1]);
        assertFalse(diff.containsKey("db.url"));
    }

    @Test
    void diffConfigShouldDetectAddedKeys() {
        Map<String, String> extendedConfig = new HashMap<>(baseConfig);
        extendedConfig.put("feature.flag", "true");

        DeploymentSnapshot snap1 = new DeploymentSnapshot(
                "snap-001", "production", "1.4.2", now, baseConfig, "alice");
        DeploymentSnapshot snap2 = new DeploymentSnapshot(
                "snap-002", "production", "1.4.3", now, extendedConfig, "alice");

        Map<String, String[]> diff = snap1.diffConfig(snap2);

        assertTrue(diff.containsKey("feature.flag"));
        assertNull(diff.get("feature.flag")[0]);
        assertEquals("true", diff.get("feature.flag")[1]);
    }

    @Test
    void diffConfigShouldReturnEmptyWhenIdentical() {
        DeploymentSnapshot snap1 = new DeploymentSnapshot(
                "snap-001", "production", "1.4.2", now, baseConfig, "alice");
        DeploymentSnapshot snap2 = new DeploymentSnapshot(
                "snap-002", "production", "1.4.2", now, new HashMap<>(baseConfig), "alice");

        assertTrue(snap1.diffConfig(snap2).isEmpty());
    }

    @Test
    void shouldThrowOnNullRequiredFields() {
        assertThrows(NullPointerException.class, () ->
                new DeploymentSnapshot(null, "production", "1.0.0", now, baseConfig, "alice"));
        assertThrows(NullPointerException.class, () ->
                new DeploymentSnapshot("snap-001", null, "1.0.0", now, baseConfig, "alice"));
        assertThrows(NullPointerException.class, () ->
                new DeploymentSnapshot("snap-001", "production", "1.0.0", now, null, "alice"));
    }
}
