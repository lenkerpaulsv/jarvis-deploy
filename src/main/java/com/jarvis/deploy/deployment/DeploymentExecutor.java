package com.jarvis.deploy.deployment;

import com.jarvis.deploy.config.EnvironmentConfig;

import java.time.Instant;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Executes deployments for a given environment and records them in history.
 */
public class DeploymentExecutor {

    private static final Logger logger = Logger.getLogger(DeploymentExecutor.class.getName());

    private final DeploymentHistory history;

    public DeploymentExecutor(DeploymentHistory history) {
        if (history == null) {
            throw new IllegalArgumentException("DeploymentHistory must not be null");
        }
        this.history = history;
    }

    /**
     * Deploys the specified artifact version to the given environment.
     *
     * @param config          the target environment configuration
     * @param artifactVersion the version string of the artifact to deploy
     * @return the DeploymentRecord created for this deployment
     * @throws DeploymentException if the deployment fails
     */
    public DeploymentRecord deploy(EnvironmentConfig config, String artifactVersion) throws DeploymentException {
        if (config == null) {
            throw new IllegalArgumentException("EnvironmentConfig must not be null");
        }
        if (artifactVersion == null || artifactVersion.isBlank()) {
            throw new IllegalArgumentException("Artifact version must not be null or blank");
        }

        String deploymentId = UUID.randomUUID().toString();
        logger.info(String.format("Starting deployment [%s] of version '%s' to environment '%s'",
                deploymentId, artifactVersion, config.getEnvironmentName()));

        try {
            runDeploymentScript(config, artifactVersion);
        } catch (Exception e) {
            throw new DeploymentException(
                    "Deployment failed for version '" + artifactVersion + "' on environment '" + config.getEnvironmentName() + "'", e);
        }

        DeploymentRecord record = new DeploymentRecord(
                deploymentId,
                config.getEnvironmentName(),
                artifactVersion,
                Instant.now()
        );
        history.addRecord(record);

        logger.info(String.format("Deployment [%s] completed successfully.", deploymentId));
        return record;
    }

    /**
     * Simulates running the deployment script. In a real implementation this
     * would invoke a shell script, SSH command, or container orchestration API.
     */
    private void runDeploymentScript(EnvironmentConfig config, String artifactVersion) {
        logger.fine(String.format("Executing deploy script: env=%s, artifact=%s, host=%s",
                config.getEnvironmentName(), artifactVersion, config.getDeployHost()));
        // Deployment logic placeholder
    }
}
