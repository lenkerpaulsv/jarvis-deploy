package com.jarvis.deploy.signal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Handles deployment lifecycle signals (pause, resume, abort, drain)
 * allowing external systems to send control signals to active deployments.
 */
public class DeploymentSignalHandler {

    private static final Logger logger = Logger.getLogger(DeploymentSignalHandler.class.getName());

    private final Map<String, DeploymentSignal> pendingSignals = new ConcurrentHashMap<>();
    private final Map<DeploymentSignalType, Consumer<DeploymentSignal>> handlers = new ConcurrentHashMap<>();

    public void registerHandler(DeploymentSignalType type, Consumer<DeploymentSignal> handler) {
        if (type == null || handler == null) {
            throw new IllegalArgumentException("Signal type and handler must not be null");
        }
        handlers.put(type, handler);
        logger.fine("Registered handler for signal type: " + type);
    }

    public void sendSignal(DeploymentSignal signal) {
        if (signal == null) {
            throw new IllegalArgumentException("Signal must not be null");
        }
        String deploymentId = signal.getDeploymentId();
        pendingSignals.put(deploymentId, signal);
        logger.info("Signal " + signal.getType() + " queued for deployment: " + deploymentId);

        Consumer<DeploymentSignal> handler = handlers.get(signal.getType());
        if (handler != null) {
            try {
                handler.accept(signal);
                pendingSignals.remove(deploymentId);
                logger.info("Signal " + signal.getType() + " processed for deployment: " + deploymentId);
            } catch (Exception e) {
                logger.warning("Error processing signal " + signal.getType() + " for deployment " + deploymentId + ": " + e.getMessage());
            }
        } else {
            logger.warning("No handler registered for signal type: " + signal.getType());
        }
    }

    public boolean hasPendingSignal(String deploymentId) {
        return pendingSignals.containsKey(deploymentId);
    }

    public DeploymentSignal getPendingSignal(String deploymentId) {
        return pendingSignals.get(deploymentId);
    }

    public void clearSignal(String deploymentId) {
        pendingSignals.remove(deploymentId);
        logger.fine("Cleared pending signal for deployment: " + deploymentId);
    }

    public int pendingCount() {
        return pendingSignals.size();
    }
}
