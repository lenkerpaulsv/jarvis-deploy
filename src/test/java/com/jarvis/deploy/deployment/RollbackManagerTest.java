package com.jarvis.deploy.deployment;

import com.jarvis.deploy.config.EnvironmentConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class RollbackManagerTest {

    private DeploymentHistory history;
    private RollbackManager rollbackManager;
    private EnvironmentConfig stagingConfig;

    @BeforeEach
    void setUp() {
        history = new DeploymentHistory();
        rollbackManager = new RollbackManager(history);
        stagingConfig = new EnvironmentConfig("staging", "localhost", 8080);
    }

    @Test
    void rollback_shouldThrowWhenNoHistoryExists() {
        RollbackException ex = assertThrows(RollbackException.class,
                () -> rollbackManager.rollback(stagingConfig));
        assertTrue(ex.getMessage().contains("staging"));
    }

    @Test
    void rollback_shouldReturnRollbackRecordWhenPreviousExists() throws RollbackException {
        DeploymentRecord first = new DeploymentRecord("1.0.0", "staging", Instant.now().minusSeconds(120), "DEPLOY");
        DeploymentRecord second = new DeploymentRecord("1.1.0", "staging", Instant.now().minusSeconds(60), "DEPLOY");
        history.addRecord(first);
        history.addRecord(second);

        DeploymentRecord result = rollbackManager.rollback(stagingConfig);

        assertNotNull(result);
        assertEquals("1.0.0", result.getVersion());
        assertEquals("staging", result.getEnvironment());
        assertEquals("ROLLBACK", result.getStatus());
    }

    @Test
    void rollback_shouldAddRollbackRecordToHistory() throws RollbackException {
        history.addRecord(new DeploymentRecord("1.0.0", "staging", Instant.now().minusSeconds(60), "DEPLOY"));
        history.addRecord(new DeploymentRecord("1.1.0", "staging", Instant.now().minusSeconds(30), "DEPLOY"));

        int sizeBefore = history.getRecordsForEnvironment("staging").size();
        rollbackManager.rollback(stagingConfig);
        int sizeAfter = history.getRecordsForEnvironment("staging").size();

        assertEquals(sizeBefore + 1, sizeAfter);
    }

    @Test
    void canRollback_shouldReturnFalseWhenNoHistory() {
        assertFalse(rollbackManager.canRollback("staging"));
    }

    @Test
    void canRollback_shouldReturnTrueWhenPreviousExists() {
        history.addRecord(new DeploymentRecord("1.0.0", "staging", Instant.now().minusSeconds(60), "DEPLOY"));
        history.addRecord(new DeploymentRecord("1.1.0", "staging", Instant.now().minusSeconds(30), "DEPLOY"));

        assertTrue(rollbackManager.canRollback("staging"));
    }

    @Test
    void constructor_shouldThrowOnNullHistory() {
        assertThrows(IllegalArgumentException. () -> new RollbackManager(null));
    }

    @Test
    void rollback_shouldThrowOnNullConfig() {
        assertThrows(IllegalArgumentException.class, () -> rollbackManager.rollback(null));
    }
}
