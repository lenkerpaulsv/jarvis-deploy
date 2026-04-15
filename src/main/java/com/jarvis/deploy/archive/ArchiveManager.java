package com.jarvis.deploy.archive;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Manages storage, retrieval, and eviction of deployment archives.
 */
public class ArchiveManager {

    private static final Logger logger = Logger.getLogger(ArchiveManager.class.getName());

    private final Map<String, DeploymentArchive> archives = new ConcurrentHashMap<>();
    private final int maxArchivesPerEnvironment;

    public ArchiveManager(int maxArchivesPerEnvironment) {
        if (maxArchivesPerEnvironment <= 0) {
            throw new IllegalArgumentException("maxArchivesPerEnvironment must be positive");
        }
        this.maxArchivesPerEnvironment = maxArchivesPerEnvironment;
    }

    public void store(DeploymentArchive archive) {
        Objects.requireNonNull(archive, "archive must not be null");
        archives.put(archive.getArchiveId(), archive);
        logger.info("Stored archive: " + archive.getArchiveId());
        evictOldestIfNeeded(archive.getEnvironment());
    }

    public Optional<DeploymentArchive> findById(String archiveId) {
        return Optional.ofNullable(archives.get(archiveId));
    }

    public List<DeploymentArchive> findByEnvironment(String environment) {
        return archives.values().stream()
                .filter(a -> a.getEnvironment().equals(environment))
                .sorted(Comparator.comparing(DeploymentArchive::getArchivedAt).reversed())
                .collect(Collectors.toList());
    }

    public boolean delete(String archiveId) {
        boolean removed = archives.remove(archiveId) != null;
        if (removed) logger.info("Deleted archive: " + archiveId);
        return removed;
    }

    public int count() {
        return archives.size();
    }

    private void evictOldestIfNeeded(String environment) {
        List<DeploymentArchive> envArchives = findByEnvironment(environment);
        if (envArchives.size() > maxArchivesPerEnvironment) {
            DeploymentArchive oldest = envArchives.get(envArchives.size() - 1);
            archives.remove(oldest.getArchiveId());
            logger.info("Evicted oldest archive for env '" + environment + "': " + oldest.getArchiveId());
        }
    }

    public Optional<DeploymentArchive> findLatestByEnvironment(String environment) {
        return findByEnvironment(environment).stream().findFirst();
    }
}
