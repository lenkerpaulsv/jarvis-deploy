package com.jarvis.deploy.cleanup;

import com.jarvis.deploy.deployment.DeploymentHistory;
import com.jarvis.deploy.deployment.DeploymentRecord;
import com.jarvis.deploy.audit.AuditLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Handles cleanup of stale deployment artifacts and expired history records.
 */
public class DeploymentCleaner {

    private static final Logger logger = Logger.getLogger(DeploymentCleaner.class.getName());

    private final DeploymentHistory deploymentHistory;
    private final AuditLogger auditLogger;
    private final int retentionDays;
    private final String artifactBaseDir;

    public DeploymentCleaner(DeploymentHistory deploymentHistory, AuditLogger auditLogger,
                              int retentionDays, String artifactBaseDir) {
        if (retentionDays <= 0) {
            throw new IllegalArgumentException("Retention days must be positive");
        }
        this.deploymentHistory = deploymentHistory;
        this.auditLogger = auditLogger;
        this.retentionDays = retentionDays;
        this.artifactBaseDir = artifactBaseDir;
    }

    public CleanupResult cleanExpiredRecords(String environment) {
        Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
        List<DeploymentRecord> all = deploymentHistory.getHistory(environment);

        List<DeploymentRecord> expired = all.stream()
                .filter(r -> r.getTimestamp().isBefore(cutoff))
                .collect(Collectors.toList());

        expired.forEach(r -> deploymentHistory.removeRecord(environment, r.getDeploymentId()));

        auditLogger.log("CLEANUP", environment,
                "Removed " + expired.size() + " expired records older than " + retentionDays + " days");

        logger.info("Cleaned " + expired.size() + " expired deployment records for environment: " + environment);
        return new CleanupResult(expired.size(), 0);
    }

    public CleanupResult cleanArtifacts(String environment) {
        Path envDir = Paths.get(artifactBaseDir, environment);
        int deletedCount = 0;

        if (!Files.exists(envDir)) {
            logger.warning("Artifact directory does not exist: " + envDir);
            return new CleanupResult(0, 0);
        }

        Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
        try {
            List<Path> stale = Files.list(envDir)
                    .filter(p -> {
                        try {
                            return Files.getLastModifiedTime(p).toInstant().isBefore(cutoff);
                        } catch (IOException e) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());

            for (Path p : stale) {
                Files.deleteIfExists(p);
                deletedCount++;
            }
        } catch (IOException e) {
            logger.severe("Failed to clean artifacts for environment " + environment + ": " + e.getMessage());
        }

        auditLogger.log("CLEANUP", environment, "Deleted " + deletedCount + " stale artifact files");
        return new CleanupResult(0, deletedCount);
    }

    public int getRetentionDays() {
        return retentionDays;
    }
}
