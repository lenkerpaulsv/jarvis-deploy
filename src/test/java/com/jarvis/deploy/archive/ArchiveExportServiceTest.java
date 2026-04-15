package com.jarvis.deploy.archive;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ArchiveExportServiceTest {

    private ArchiveManager archiveManager;
    private ArchiveExportService exportService;

    @BeforeEach
    void setUp() {
        archiveManager = new ArchiveManager(10);
        exportService = new ArchiveExportService(archiveManager);
    }

    private DeploymentArchive makeArchive(String id, String env, String version) {
        return new DeploymentArchive(id, env, "/artifacts/" + id + ".jar",
                version, Instant.now(), Map.of());
    }

    @Test
    void exportSummaryContainsEnvironmentHeader() {
        archiveManager.store(makeArchive("arc-001", "staging", "1.0.0"));
        String summary = exportService.exportSummary("staging");
        assertTrue(summary.contains("staging"));
        assertTrue(summary.contains("1.0.0"));
    }

    @Test
    void exportSummaryEmptyEnvironment() {
        String summary = exportService.exportSummary("unknown-env");
        assertTrue(summary.contains("No archives found"));
    }

    @Test
    void exportArchiveIdsReturnsCorrectIds() {
        archiveManager.store(makeArchive("arc-001", "prod", "1.0.0"));
        archiveManager.store(makeArchive("arc-002", "prod", "2.0.0"));
        archiveManager.store(makeArchive("arc-003", "staging", "1.0.0"));
        List<String> ids = exportService.exportArchiveIds("prod");
        assertEquals(2, ids.size());
        assertTrue(ids.contains("arc-001"));
        assertTrue(ids.contains("arc-002"));
    }

    @Test
    void exportAllIncludesTotalCount() {
        archiveManager.store(makeArchive("arc-001", "prod", "1.0.0"));
        archiveManager.store(makeArchive("arc-002", "staging", "1.0.0"));
        String result = exportService.exportAll();
        assertTrue(result.contains("Total archives: 2"));
    }

    @Test
    void nullArchiveManagerThrows() {
        assertThrows(NullPointerException.class, () -> new ArchiveExportService(null));
    }
}
