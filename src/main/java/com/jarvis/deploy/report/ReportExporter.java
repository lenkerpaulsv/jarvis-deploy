package com.jarvis.deploy.report;

import java.util.List;

public class ReportExporter {

    public String exportAsText(DeploymentReport report) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Deployment Report ===").append("\n");
        sb.append("ID          : ").append(report.getReportId()).append("\n");
        sb.append("Environment : ").append(report.getEnvironment()).append("\n");
        sb.append("Generated   : ").append(report.getGeneratedAt()).append("\n");
        sb.append("\n--- Summary ---\n");
        report.getSummary().forEach((k, v) -> sb.append(k).append(": ").append(v).append("\n"));
        sb.append("\n--- Entries ---\n");
        for (ReportEntry entry : report.getEntries()) {
            sb.append(String.format("[%s] %s | %s | by %s | %ds%n",
                    entry.getStatus(), entry.getDeploymentId(),
                    entry.getArtifactVersion(), entry.getDeployedBy(),
                    entry.getDurationSeconds()));
        }
        return sb.toString();
    }

    public String exportAsCsv(DeploymentReport report) {
        StringBuilder sb = new StringBuilder();
        sb.append("deploymentId,artifactVersion,status,deployedAt,deployedBy,durationSeconds\n");
        for (ReportEntry entry : report.getEntries()) {
            sb.append(String.join(",",
                    entry.getDeploymentId(),
                    entry.getArtifactVersion(),
                    entry.getStatus(),
                    entry.getDeployedAt().toString(),
                    entry.getDeployedBy(),
                    String.valueOf(entry.getDurationSeconds())));
            sb.append("\n");
        }
        return sb.toString();
    }
}
