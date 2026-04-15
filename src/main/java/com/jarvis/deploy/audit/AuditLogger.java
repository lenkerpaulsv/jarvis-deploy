package com.jarvis.deploy.audit;

import com.jarvis.deploy.deployment.DeploymentRecord;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Appends structured audit log entries for deployment events to a persistent log file.
 */
public class AuditLogger {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Path logFilePath;

    public AuditLogger(String logFilePath) {
        this.logFilePath = Paths.get(logFilePath);
    }

    public void log(AuditEntry entry) throws IOException {
        ensureFileExists();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFilePath.toFile(), true))) {
            writer.write(formatEntry(entry));
            writer.newLine();
        }
    }

    public void logDeployment(DeploymentRecord record, String action, String performedBy) throws IOException {
        AuditEntry entry = new AuditEntry(
                action,
                record.getEnvironment(),
                record.getVersion(),
                performedBy,
                record.getTimestamp()
        );
        log(entry);
    }

    public List<String> readAll() throws IOException {
        if (!Files.exists(logFilePath)) {
            return new ArrayList<>();
        }
        return Files.readAllLines(logFilePath);
    }

    private String formatEntry(AuditEntry entry) {
        return String.format("[%s] ACTION=%s ENV=%s VERSION=%s USER=%s",
                entry.getTimestamp().format(FORMATTER),
                entry.getAction(),
                entry.getEnvironment(),
                entry.getVersion(),
                entry.getPerformedBy());
    }

    private void ensureFileExists() throws IOException {
        if (!Files.exists(logFilePath)) {
            Files.createDirectories(logFilePath.getParent() != null ? logFilePath.getParent() : Paths.get("."));
            Files.createFile(logFilePath);
        }
    }
}
