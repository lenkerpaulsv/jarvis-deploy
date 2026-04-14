package com.jarvis.deploy.deployment;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Manages persistent deployment history for rollback support.
 * Records are stored in a simple CSV-style flat file per environment.
 */
public class DeploymentHistory {

    private static final String HISTORY_DIR = ".jarvis/history";
    private static final String FILE_SUFFIX = "-deployments.csv";
    private static final String CSV_HEADER = "id,environment,artifact,version,timestamp,status";

    private final String environment;
    private final Path historyFile;

    public DeploymentHistory(String environment) {
        this.environment = environment;
        this.historyFile = Paths.get(HISTORY_DIR, environment + FILE_SUFFIX);
    }

    public void record(DeploymentRecord record) throws IOException {
        Files.createDirectories(historyFile.getParent());
        boolean isNew = !Files.exists(historyFile);
        try (BufferedWriter writer = Files.newBufferedWriter(historyFile,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            if (isNew) {
                writer.write(CSV_HEADER);
                writer.newLine();
            }
            writer.write(toCsv(record));
            writer.newLine();
        }
    }

    public List<DeploymentRecord> loadAll() throws IOException {
        if (!Files.exists(historyFile)) {
            return Collections.emptyList();
        }
        List<DeploymentRecord> records = new ArrayList<>();
        List<String> lines = Files.readAllLines(historyFile);
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (!line.isEmpty()) {
                records.add(fromCsv(line));
            }
        }
        return Collections.unmodifiableList(records);
    }

    public Optional<DeploymentRecord> getLatestSuccessful() throws IOException {
        List<DeploymentRecord> all = loadAll();
        for (int i = all.size() - 1; i >= 0; i--) {
            if ("SUCCESS".equalsIgnoreCase(all.get(i).getStatus())) {
                return Optional.of(all.get(i));
            }
        }
        return Optional.empty();
    }

    private String toCsv(DeploymentRecord r) {
        return String.join(",",
                r.getId(), r.getEnvironment(), r.getArtifact(),
                r.getVersion(), r.getTimestamp().toString(), r.getStatus());
    }

    private DeploymentRecord fromCsv(String line) {
        String[] parts = line.split(",", 6);
        return new DeploymentRecord(
                parts[0], parts[1], parts[2],
                parts[3], LocalDateTime.parse(parts[4]), parts[5]);
    }
}
