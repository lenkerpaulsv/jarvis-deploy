package com.jarvis.deploy.deployment;

import com.jarvis.deploy.config.EnvironmentConfig;

import java.time.Instant;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Manages rollback operations for a given environment.
 * Uses DeploymentHistory to find the previous stable deployment
 * and re-deploys it.
 */
public class RollbackManager {

    private static final Logger LOGGER = Logger.getLogger(RollbackManager.class.getName());

    private final DeploymentHistory history;

    public RollbackManager(DeploymentHistory history) {
        if (history == null) {
            throw new IllegalArgumentException("DeploymentHistory must not be null");
        }
        this.history = history;
    }

    /**
     * Rolls back the given environment to its most recent previous deployment.
     *
     * @param config the target environment configuration
     * @return the DeploymentRecord that was rolled back to
     * @throws RollbackException if no previous deployment is available
     */
    public DeploymentRecord rollback(EnvironmentConfig config) throws RollbackException {
        if (config == null) {
            throw new IllegalArgumentException("EnvironmentConfig must not be null");
        }

        String env = config.getEnvironment();
        LOGGER.info("Initiating rollback for environment: " + env);

        Optional<DeploymentRecord> previous = history.getPreviousDeployment(env);

        if (previous.isEmpty()) {
            throw new RollbackException("No previous deployment found for environment: " + env);
        }

        DeploymentRecord target = previous.get();
        LOGGER.info(String.format("Rolling back environment '%s' to version '%s' (deployed at %s)",
                env, target.getVersion(), target.getDeployedAt()));

        DeploymentRecord rollbackRecord = new DeploymentRecord(
                target.getVersion(),
                env,
                Instant.now(),
                "ROLLBACK"
        );

        history.addRecord(rollbackRecord);
        LOGGER.info("Rollback complete for environment: " + env);
        return rollbackRecord;
    }

    /**
     * Checks whether a rollback is possible for the given environment.
     */
    public boolean canRollback(String environment) {
        return history.getPreviousDeployment(environment).isPresent();
    }
}
