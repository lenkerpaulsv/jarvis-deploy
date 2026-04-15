package com.jarvis.deploy.cli;

import com.jarvis.deploy.config.ConfigLoader;
import com.jarvis.deploy.config.EnvironmentConfig;
import com.jarvis.deploy.deployment.DeploymentExecutor;
import com.jarvis.deploy.deployment.DeploymentHistory;
import com.jarvis.deploy.deployment.DeploymentRecord;
import com.jarvis.deploy.deployment.RollbackManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

imito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommandDispatcherTest {

    @Mock private ConfigLoader configLoader;
    @Mock private DeploymentExecutor executor;
    @Mock private RollbackManager rollbackManager;
    @Mock private DeploymentHistory history;

    private CommandDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        dispatcher = new CommandDispatcher(configLoader,;
    }

    @Test
    void dispatch_deployCommand_executesDeployment() {
        EnvironmentConfig config = mock(EnvironmentConfig.class);
        when(configLoader.load("staging")).thenReturn(config);

        dispatcher.dispatch(new String[]{"deploy", "staging", "app-1.0.jar"});

        verify(configLoader).load("staging");
        verify(executor).execute(config, "app-1.0.jar");
    }

    @Test
    void dispatch_rollbackCommand_triggersRollback() {
        dispatcher.dispatch(new String[]{"rollback", "production"});

        verify(rollbackManager).rollback("production");
    }

    @Test
    void dispatch_historyCommand_printsRecords() {
        DeploymentRecord record = mock(DeploymentRecord.class);
        when(record.toString()).thenReturn("[production] app-1.0.jar SUCCESS");
        when(history.getRecords("production")).thenReturn(List.of(record));

        dispatcher.dispatch(new String[]{"history", "production"});

        verify(history).getRecords("production");
    }

    @Test
    void dispatch_historyCommand_emptyHistory_printsMessage() {
        when(history.getRecords("dev")).thenReturn(Collections.emptyList());

        dispatcher.dispatch(new String[]{"history", "dev"});

        verify(history).getRecords("dev");
        verifyNoInteractions(executor, rollbackManager);
    }

    @Test
    void dispatch_unknownCommand_doesNotCallServices() {
        dispatcher.dispatch(new String[]{"invalid"});

        verifyNoInteractions(executor, rollbackManager, history, configLoader);
    }

    @Test
    void dispatch_nullArgs_doesNotThrow() {
        dispatcher.dispatch(null);

        verifyNoInteractions(executor, rollbackManager, history, configLoader);
    }

    @Test
    void dispatch_deployMissingArgs_doesNotCallExecutor() {
        dispatcher.dispatch(new String[]{"deploy", "staging"});

        verifyNoInteractions(executor);
    }
}
