package com.jarvis.deploy.cli;

import com.jarvis.deploy.config.ConfigLoader;
import com.jarvis.deploy.deployment.DeploymentExecutor;
import com.jarvis.deploy.deployment.DeploymentHistory;
import com.jarvis.deploy.deployment.DeploymentValidator;
import com.jarvis.deploy.deployment.RollbackManager;
import com.jarvis.deploy.notification.DeploymentNotifier;

/**
 * Entry point for the jarvis-deploy CLI application.
 * Wires up dependencies and delegates to CommandDispatcher.
 */
public class CliRunner {

    public static void main(String[] args) {
        CliRunner runner = new CliRunner();
        runner.run(args);
    }

    public void run(String[] args) {
        ConfigLoader configLoader = new ConfigLoader();
        DeploymentHistory history = new DeploymentHistory();
        DeploymentNotifier notifier = new DeploymentNotifier();
        DeploymentValidator validator = new DeploymentValidator();
        DeploymentExecutor executor = new DeploymentExecutor(validator, history, notifier);
        RollbackManager rollbackManager = new RollbackManager(history, executor, notifier);

        CommandDispatcher dispatcher = new CommandDispatcher(
                configLoader, executor, rollbackManager, history);

        try {
            dispatcher.dispatch(args);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
}
