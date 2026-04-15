package com.jarvis.deploy.status;

import com.jarvis.deploy.config.EnvironmentConfig;
import com.jarvis.deploy.deployment.DeploymentRecord;

import java.io.PrintStream;
import java.util.List;
import java.util.Optional;

/**
 * Formats and prints deployment status reports to a given output stream.
 */
public class StatusReporter {

    private final DeploymentStatus deploymentStatus;
    private final PrintStream out;

    public StatusReporter(DeploymentStatus deploymentStatus, PrintStream out) {
        if (deploymentStatus == null) throw new IllegalArgumentException("DeploymentStatus must not be null");
        if (out == null) throw new IllegalArgumentException("PrintStream must not be null");
        this.deploymentStatus = deploymentStatus;
        this.out = out;
    }

    /**
     * Prints a single-environment status report.
     */
    public void report(String environment) {
        String summary = deploymentStatus.getSummary(environment);
        boolean healthy = deploymentStatus.isHealthy(environment);
        out.println(summary);
        out.println("Health: " + (healthy ? "OK" : "DEGRADED"));
    }

    /**
     * Prints status reports for all provided environments.
     */
    public void reportAll(List<String> environments) {
        if (environments == null || environments.isEmpty()) {
            out.println("No environments configured.");
            return;
        }
        out.println("=== Deployment Status Report ===");
        for (String env : environments) {
            report(env);
            out.println("-------------------------------");
        }
    }

    /**
     * Prints a compact one-liner health check for each environment.
     */
    public void healthCheck(List<String> environments) {
        if (environments == null || environments.isEmpty()) {
            out.println("No environments to check.");
            return;
        }
        for (String env : environments) {
            boolean healthy = deploymentStatus.isHealthy(env);
            out.printf("%-20s %s%n", env, healthy ? "[OK]" : "[DEGRADED]");
        }
    }
}
