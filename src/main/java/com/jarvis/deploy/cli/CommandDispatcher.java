package com.jarvis.deploy.cli;

import com.jarvis.deploy.config.ConfigLoader;
import com.jarvis.deploy.config.EnvironmentConfig;
import com.jarvis.deploy.deployment.DeploymentExecutor;
import com.jarvis.deploy.deployment.DeploymentHistory;
import com.jarvis.deploy.deployment.DeploymentRecord;
import com.jarvis.deploy.deployment.RollbackManager;

import java.util.List;

/**
 * Dispatches CLI commands to the appropriate deployment operations.
 */
public class CommandDispatcher {

    private final ConfigLoader configLoader;
    private final DeploymentExecutor executor;
    private final RollbackManager rollbackManager;
    private final DeploymentHistory history;

    public CommandDispatcher(ConfigLoader configLoader,
                             DeploymentExecutor executor,
                             RollbackManager rollbackManager,
                             DeploymentHistory history) {
        this.configLoader = configLoader;
        this.executor = executor;
        this.rollbackManager = rollbackManager;
        this.history = history;
    }

    public void dispatch(String[] args) {
        if (args == null || args.length == 0) {
            printUsage();
            return;
        }

        String command = args[0].toLowerCase();

        switch (command) {
            case "deploy":
                handleDeploy(args);
                break;
            case "rollback":
                handleRollback(args);
                break;
            case "history":
                handleHistory(args);
                break;
            default:
                System.err.println("Unknown command: " + command);
                printUsage();
        }
    }

    private void handleDeploy(String[] args) {
        if (args.length < 3) {
            System.err.println("Usage: deploy <environment> <artifact>");
            return;
        }
        String env = args[1];
        String artifact = args[2];
        EnvironmentConfig config = configLoader.load(env);
        executor.execute(config, artifact);
    }

    private void handleRollback(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: rollback <environment>");
            return;
        }
        String env = args[1];
        rollbackManager.rollback(env);
    }

    private void handleHistory(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: history <environment>");
            return;
        }
        String env = args[1];
        List<DeploymentRecord> records = history.getRecords(env);
        if (records.isEmpty()) {
            System.out.println("No deployment history for environment: " + env);
        } else {
            records.forEach(r -> System.out.println(r.toString()));
        }
    }

    private void printUsage() {
        System.out.println("jarvis-deploy CLI");
        System.out.println("Commands:");
        System.out.println("  deploy <environment> <artifact>  - Deploy artifact to environment");
        System.out.println("  rollback <environment>           - Rollback last deployment");
        System.out.println("  history <environment>            - Show deployment history");
    }
}
