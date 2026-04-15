package com.jarvis.deploy.archive;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ArchiveManagerTest {

    private ArchiveManager archiveManager;

    @BeforeEach
    void setUp() {
        archiveManager = new ArchiveManager(3);
    }

    private DeploymentArchive makeArchive(String id, String env, String version) {
        return new DeploymentArchive(id, env, "/artifacts/" + id + ".jar", version,
                Instant.now(), Map.of("build", "ci-123"));
    }

    @Test
    void storeAndFindById() {
        DeploymentArchive archive = makeArchive("arc-001", "staging", "1.0.0");
        archiveManager.store(archive);
        Optional<DeploymentArchive> found = archiveManager.findById("arc-001");
        assertTrue(found.isPresent());
        assertEquals("1.0.0", found.get().getVersion());
    }

    @Test
    void findByEnvironmentReturnsSortedDescending() throws InterruptedException {
        archiveManager.store(makeArchive("arc-001", "prod", "1.0.0"));
        Thread.sleep(5);
        archiveManager.store(makeArchive("arc-002", "prod", "1.1.0"));
        List<DeploymentArchive> results = archiveManager.findByEnvironment("prod");
        assertEquals(2, results.size());
        assertEquals("arc-002", results.get(0).getArchiveId());
    }

    @Test
    void evictsOldestWhenLimitExceeded() throws InterruptedException {
        archiveManager.store(makeArchive("arc-001", "dev", "1.0.0"));
        Thread.sleep(5);
        archiveManager.store(makeArchive("arc-002", "dev", "1.1.0"));
        Thread.sleep(5);
        archiveManager.store(makeArchive("arc-003", "dev", "1.2.0"));
        Thread.sleep(5);
        archiveManager.store(makeArchive("arc-004", "dev", "1.3.0"));
        List<DeploymentArchive> results = archiveManager.findByEnvironment("dev");
        assertEquals(3, results.size());
        assertTrue(results.stream().noneMatch(a -> a.getArchiveId().equals("arc-001")));
    }

    @Test
    void deleteRemovesArchive() {
        archiveManager.store(makeArchive("arc-001", "staging", "1.0.0"));
        assertTrue(archiveManager.delete("arc-001"));
        assertFalse(archiveManager.findById("arc-001").isPresent());
    }

    @Test
    void findLatestByEnvironmentReturnsNewest() throws InterruptedException {
        archiveManager.store(makeArchive("arc-001", "prod", "1.0.0"));
        Thread.sleep(5);
        archiveManager.store(makeArchive("arc-002", "prod", "2.0.0"));
        Optional<DeploymentArchive> latest = archiveManager.findLatestByEnvironment("prod");
        assertTrue(latest.isPresent());
        assertEquals("2.0.0", latest.get().getVersion());
    }

    @Test
    void invalidMaxThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new ArchiveManager(0));
    }
}
