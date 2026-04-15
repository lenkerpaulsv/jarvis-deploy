package com.jarvis.deploy.deployment;

/**
 * Thrown when a deployment operation fails.
 */
public class DeploymentException extends Exception {

    public DeploymentException(String message) {
        super(message);
    }

    public DeploymentException(String message, Throwable cause) {
        super(message, cause);
    }
}
