package com.jarvis.deploy.deployment;

import com.jarvis.deploy.config.EnvironmentConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Validates deployment prerequisites before executing a deployment.
 */
public class DeploymentValidator {

    private static final long MAX_ARTIFACT_SIZE_BYTES = 500 * 1024 * 1024; // 500 MB

    /**
     * Validates the given artifact path and environment configuration.
     *
     * @param artifactPath path to the artifact to deploy
     * @param config       target environment configuration
     * @throws DeploymentException if any validation check fails
     */
    public void validate(String artifactPath, EnvironmentConfig config) throws DeploymentException {
        List<String> errors = new ArrayList<>();

        validateArtifact(artifactPath, errors);
        validateEnvironmentConfig(config, errors);

        if (!errors.isEmpty()) {
            throw new DeploymentException(
                "Deployment validation failed:\n  - " + String.join("\n  - ", errors)
            );
        }
    }

    private void validateArtifact(String artifactPath, List<String> errors) {
        if (artifactPath == null || artifactPath.isBlank()) {
            errors.add("Artifact path must not be null or empty");
            return;
        }

        File artifact = new File(artifactPath);

        if (!artifact.exists()) {
            errors.add("Artifact not found: " + artifactPath);
            return;
        }

        if (!artifact.isFile()) {
            errors.add("Artifact path does not point to a file: " + artifactPath);
            return;
        }

        if (!artifactPath.endsWith(".jar") && !artifactPath.endsWith(".war")) {
            errors.add("Artifact must be a .jar or .war file: " + artifactPath);
        }

        if (artifact.length() > MAX_ARTIFACT_SIZE_BYTES) {
            errors.add("Artifact exceeds maximum allowed size of 500 MB: " + artifactPath);
        }
    }

    private void validateEnvironmentConfig(EnvironmentConfig config, List<String> errors) {
        if (config == null) {
            errors.add("Environment configuration must not be null");
            return;
        }

        if (config.getName() == null || config.getName().isBlank()) {
            errors.add("Environment name must not be null or empty");
        }

        if (config.getDeployPath() == null || config.getDeployPath().isBlank()) {
            errors.add("Deploy path must not be null or empty");
        }

        if (config.getHost() == null || config.getHost().isBlank()) {
            errors.add("Host must not be null or empty");
        }
    }
}
