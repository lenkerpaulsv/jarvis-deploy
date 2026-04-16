package com.jarvis.deploy.report;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ReportGenerator {

    public DeploymentReport generate(String environment, List<ReportEntry> entries) {
        if (environment == null || environment.isBlank()) {
            throw new IllegalArgumentException("Environment must not be blank");
        }
        if (entries == null) {
            throw new IllegalArgumentException("Entries must not be null");
        }

        String reportId = UUID.randomUUID().toString();
        Map<String, String> summary = buildSummary(entries);
        return new DeploymentReport(reportId, environment, new ArrayList<>(entries), summary);
    }

    private Map<String, String> buildSummary(List<ReportEntry> entries) {
        Map<String, String> summary = new HashMap<>();
        long success = entries.stream().filter(e -> "SUCCESS".equalsIgnoreCase(e.getStatus())).count();
        long failed = entries.stream().filter(e -> "FAILED".equalsIgnoreCase(e.getStatus())).count();
        long rolledBack = entries.stream().filter(e -> "ROLLED_BACK".equalsIgnoreCase(e.getStatus())).count();
        OptionalDouble avg = entries.stream().mapToLong(ReportEntry::getDurationSeconds).average();

        summary.put("total", String.valueOf(entries.size()));
        summary.put("success", String.valueOf(success));
        summary.put("failed", String.valueOf(failed));
        summary.put("rolledBack", String.valueOf(rolledBack));
        summary.put("avgDurationSeconds", avg.isPresent() ? String.format("%.1f", avg.getAsDouble()) : "N/A");
        return summary;
    }
}
