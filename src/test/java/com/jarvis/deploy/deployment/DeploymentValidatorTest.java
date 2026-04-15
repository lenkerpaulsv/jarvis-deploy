package com.jarvis.deploy.deployment;

import com.jarvis.deploy.config.EnvironmentConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentValidatorTest {

    private DeploymentValidator validator;
    private EnvironmentConfig validConfig;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        validator = new DeploymentValidator();
        validConfig = new EnvironmentConfig();
        validConfig.setName("staging");
        validConfig.setHost("staging.example.com");
        validConfig.setDeployPath("/opt/app/staging");
    }

    @Test
    void validate_withValidArtifactAndConfig_doesNotThrow() throws IOException, DeploymentException {
        File artifact = tempDir.resolve("app.jar").toFile();
        assertTrue(artifact.createNewFile());

        assertDoesNotThrow(() -> validator.validate(artifact.getAbsolutePath(), validConfig));
    }

    @Test
    void validate_withNullArtifactPath_throwsDeploymentException() {
        DeploymentException ex = assertThrows(DeploymentException.class,
            () -> validator.validate(null, validConfig));
        assertTrue(ex.getMessage().contains("Artifact path must not be null or empty"));
    }

    @Test
    void validate_withNonExistentArtifact_throwsDeploymentException() {
        DeploymentException ex = assertThrows(DeploymentException.class,
            () -> validator.validate("/nonexistent/path/app.jar", validConfig));
        assertTrue(ex.getMessage().contains("Artifact not found"));
    }

    @Test
    void validate_withInvalidArtifactExtension_throwsDeploymentException() throws IOException {
        File artifact = tempDir.resolve("app.zip").toFile();
        assertTrue(artifact.createNewFile());

        DeploymentException ex = assertThrows(DeploymentException.class,
            () -> validator.validate(artifact.getAbsolutePath(), validConfig));
        assertTrue(ex.getMessage().contains("must be a .jar or .war file"));
    }

    @Test
    void validate_withNullConfig_throwsDeploymentException() throws IOException {
        File artifact = tempDir.resolve("app.jar").toFile();
        assertTrue(artifact.createNewFile());

        DeploymentException ex = assertThrows(DeploymentException.class,
            () -> validator.validate(artifact.getAbsolutePath(), null));
        assertTrue(ex.getMessage().contains("Environment configuration must not be null"));
    }

    @Test
    void validate_withMissingHost_throwsDeploymentException() throws IOException {
        File artifact = tempDir.resolve("app.jar").toFile();
        assertTrue(artifact.createNewFile());

        validConfig.setHost(null);

        DeploymentException ex = assertThrows(DeploymentException.class,
            () -> validator.validate(artifact.getAbsolutePath(), validConfig));
        assertTrue(ex.getMessage().contains("Host must not be null or empty"));
    }

    @Test
    void validate_withWarArtifact_doesNotThrow() throws IOException, DeploymentException {
        File artifact = tempDir.resolve("app.war").toFile();
        assertTrue(artifact.createNewFile());

        assertDoesNotThrow(() -> validator.validate(artifact.getAbsolutePath(), validConfig));
    }
}
