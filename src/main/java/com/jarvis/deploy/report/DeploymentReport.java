package com.jarvis.deploy.report;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DeploymentReport {
    private final String reportId;
    private final String environment;
    private final Instant generatedAt;
    private final List<ReportEntry> entries;
    private final Map<String, String> summary;

    public DeploymentReport(String reportId, String environment, List<ReportEntry> entries, Map<String, String> summary) {
        if (reportId == null || reportId.isBlank()) throw new IllegalArgumentException("reportId must not be blank");
        if (environment == null || environment.isBlank()) throw new IllegalArgumentException("environment must not be blank");
        this.reportId = reportId;
        this.environment = environment;
        this.generatedAt = Instant.now();
        this.entries = Collections.unmodifiableList(entries);
        this.summary = Collections.unmodifiableMap(summary);
    }

    public String getReportId() { return reportId; }
    public String getEnvironment() { return environment; }
    public Instant getGeneratedAt() { return generatedAt; }
    public List<ReportEntry> getEntries() { return entries; }
    public Map<String, String> getSummary() { return summary; }

    public int getTotalDeployments() { return entries.size(); }

    public long countByStatus(String status) {
        return entries.stream().filter(e -> status.equalsIgnoreCase(e.getStatus())).count();
    }

    @Override
    public String toString() {
        return "DeploymentReport[" + reportId + ", env=" + environment + ", entries=" + entries.size() + "]";
    }
}
