package com.jarvis.deploy.deployment;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentHistoryTest {

    @TempDir
    Path tempDir;

    private DeploymentHistory history;

    @BeforeEach
    void setUp() throws Exception {
        history = new DeploymentHistory("staging");
        // Redirect history file to temp directory for isolation
        Field field = DeploymentHistory.class.getDeclaredField("historyFile");
        field.setAccessible(true);
        field.set(history, tempDir.resolve("staging-deployments.csv"));
    }

    private DeploymentRecord makeRecord(String id, String version, String status) {
        return new DeploymentRecord(id, "staging", "app.jar", version,
                LocalDateTime.of(2024, 6, 1, 12, 0, 0), status);
    }

    @Test
    void loadAll_returnsEmpty_whenNoHistoryFile() throws IOException {
        List<DeploymentRecord> records = history.loadAll();
        assertTrue(records.isEmpty(), "Expected empty list when no history file exists");
    }

    @Test
    void record_and_loadAll_singleEntry() throws IOException {
        DeploymentRecord rec = makeRecord("dep-001", "1.0.0", "SUCCESS");
        history.record(rec);

        List<DeploymentRecord> records = history.loadAll();
        assertEquals(1, records.size());
        assertEquals("dep-001", records.get(0).getId());
        assertEquals("1.0.0", records.get(0).getVersion());
        assertEquals("SUCCESS", records.get(0).getStatus());
    }

    @Test
    void record_appendsMultipleEntries() throws IOException {
        history.record(makeRecord("dep-001", "1.0.0", "SUCCESS"));
        history.record(makeRecord("dep-002", "1.1.0", "FAILED"));
        history.record(makeRecord("dep-003", "1.2.0", "SUCCESS"));

        List<DeploymentRecord> records = history.loadAll();
        assertEquals(3, records.size());
        assertEquals("dep-002", records.get(1).getId());
    }

    @Test
    void getLatestSuccessful_returnsLastSuccessRecord() throws IOException {
        history.record(makeRecord("dep-001", "1.0.0", "SUCCESS"));
        history.record(makeRecord("dep-002", "1.1.0", "FAILED"));
        history.record(makeRecord("dep-003", "1.2.0", "SUCCESS"));
        history.record(makeRecord("dep-004", "1.3.0", "FAILED"));

        Optional<DeploymentRecord> latest = history.getLatestSuccessful();
        assertTrue(latest.isPresent());
        assertEquals("dep-003", latest.get().getId());
        assertEquals("1.2.0", latest.get().getVersion());
    }

    @Test
    void getLatestSuccessful_returnsEmpty_whenNoSuccessfulDeployments() throws IOException {
        history.record(makeRecord("dep-001", "1.0.0", "FAILED"));
        Optional<DeploymentRecord> latest = history.getLatestSuccessful();
        assertFalse(latest.isPresent());
    }
}
