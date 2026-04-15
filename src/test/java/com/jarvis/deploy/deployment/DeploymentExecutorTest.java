package com.jarvis.deploy.deployment;

import com.jarvis.deploy.config.EnvironmentConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentExecutorTest {

    private DeploymentHistory history;
    private DeploymentExecutor executor;
    private EnvironmentConfig config;

    @BeforeEach
    void setUp() {
        history = new DeploymentHistory();
        executor = new DeploymentExecutor(history);
        config = new EnvironmentConfig("staging", "staging.example.com", 8080);
    }

    @Test
    void deploy_shouldReturnRecordWithCorrectVersionAndEnvironment() throws DeploymentException {
        DeploymentRecord record = executor.deploy(config, "1.2.3");

        assertNotNull(record);
        assertEquals("staging", record.getEnvironmentName());
        assertEquals("1.2.3", record.getArtifactVersion());
        assertNotNull(record.getDeploymentId());
        assertNotNull(record.getTimestamp());
    }

    @Test
    void deploy_shouldAddRecordToHistory() throws DeploymentException {
        assertTrue(history.getRecords("staging").isEmpty());

        executor.deploy(config, "2.0.0");

        assertEquals(1, history.getRecords("staging").size());
        assertEquals("2.0.0", history.getRecords("staging").get(0).getArtifactVersion());
    }

    @Test
    void deploy_multipleVersions_shouldPreserveOrder() throws DeploymentException {
        executor.deploy(config, "1.0.0");
        executor.deploy(config, "1.1.0");
        executor.deploy(config, "1.2.0");

        var records = history.getRecords("staging");
        assertEquals(3, records.size());
        assertEquals("1.0.0", records.get(0).getArtifactVersion());
        assertEquals("1.2.0", records.get(2).getArtifactVersion());
    }

    @Test
    void deploy_nullConfig_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> executor.deploy(null, "1.0.0"));
    }

    @Test
    void deploy_blankVersion_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> executor.deploy(config, "  "));
    }

    @Test
    void deploy_nullVersion_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> executor.deploy(config, null));
    }

    @Test
    void constructor_nullHistory_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new DeploymentExecutor(null));
    }
}
