package com.jarvis.deploy.archive;

import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Exports archive summaries for reporting and audit purposes.
 */
public class ArchiveExportService {

    private static final Logger logger = Logger.getLogger(ArchiveExportService.class.getName());

    private final ArchiveManager archiveManager;

    public ArchiveExportService(ArchiveManager archiveManager) {
        this.archiveManager = Objects.requireNonNull(archiveManager, "archiveManager must not be null");
    }

    public String exportSummary(String environment) {
        Objects.requireNonNull(environment, "environment must not be null");
        List<DeploymentArchive> archives = archiveManager.findByEnvironment(environment);
        if (archives.isEmpty()) {
            return "No archives found for environment: " + environment;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("=== Archive Summary for '").append(environment).append("' ===\n");
        archives.forEach(a -> sb.append(String.format(
                "  [%s] version=%s artifact=%s archivedAt=%s%n",
                a.getArchiveId(), a.getVersion(), a.getArtifactPath(), a.getArchivedAt())));
        logger.info("Exported archive summary for environment: " + environment);
        return sb.toString();
    }

    public List<String> exportArchiveIds(String environment) {
        return archiveManager.findByEnvironment(environment).stream()
                .map(DeploymentArchive::getArchiveId)
                .collect(Collectors.toList());
    }

    public String exportAll() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Full Archive Export ===\n");
        sb.append("Total archives: ").append(archiveManager.count()).append("\n");
        return sb.toString();
    }
}
