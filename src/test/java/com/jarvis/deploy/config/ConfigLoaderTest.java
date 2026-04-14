package com.jarvis.deploy.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConfigLoaderTest {

    private final ConfigLoader loader = new ConfigLoader();

    @TempDir
    Path tempDir;

    @Test
    void loadsValidEnvironments() throws IOException {
        Path config = tempDir.resolve("deploy.properties");
        Files.writeString(config,
            "env.staging.host=staging.example.com\n" +
            "env.staging.port=2222\n" +
            "env.staging.deployPath=/opt/app/staging\n" +
            "env.staging.user=deployer\n" +
            "env.production.host=prod.example.com\n" +
            "env.production.deployPath=/opt/app/prod\n" +
            "env.production.user=deploy\n"
        );

        Map<String, EnvironmentConfig> result = loader.load(config);

        assertEquals(2, result.size());
        EnvironmentConfig staging = result.get("staging");
        assertNotNull(staging);
        assertEquals("staging.example.com", staging.getHost());
        assertEquals(2222, staging.getPort());
        assertEquals("/opt/app/staging", staging.getDeployPath());
        assertEquals("deployer", staging.getUser());

        EnvironmentConfig prod = result.get("production");
        assertNotNull(prod);
        assertEquals(22, prod.getPort(), "Default port should be 22");
    }

    @Test
    void skipsEnvironmentWithMissingRequiredField() throws IOException {
        Path config = tempDir.resolve("deploy.properties");
        Files.writeString(config,
            "env.broken.host=broken.example.com\n" +
            "env.broken.deployPath=/opt/app\n" +
            // missing user
            "env.valid.host=valid.example.com\n" +
            "env.valid.deployPath=/opt/valid\n" +
            "env.valid.user=admin\n"
        );

        Map<String, EnvironmentConfig> result = loader.load(config);

        assertEquals(1, result.size());
        assertTrue(result.containsKey("valid"));
        assertFalse(result.containsKey("broken"));
    }

    @Test
    void loadsEnvVars() throws IOException {
        Path config = tempDir.resolve("deploy.properties");
        Files.writeString(config,
            "env.dev.host=dev.local\n" +
            "env.dev.deployPath=/tmp/app\n" +
            "env.dev.user=devuser\n" +
            "env.dev.var.JAVA_OPTS=-Xmx512m\n" +
            "env.dev.var.APP_ENV=development\n"
        );

        Map<String, EnvironmentConfig> result = loader.load(config);
        EnvironmentConfig dev = result.get("dev");
        assertNotNull(dev);
        assertEquals("-Xmx512m", dev.getEnvVars().get("JAVA_OPTS"));
        assertEquals("development", dev.getEnvVars().get("APP_ENV"));
    }
}
