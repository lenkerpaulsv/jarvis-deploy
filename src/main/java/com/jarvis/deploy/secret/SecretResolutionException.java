package com.jarvis.deploy.secret;

/**
 * Thrown when a secret placeholder cannot be resolved during deployment configuration processing.
 */
public class SecretResolutionException extends RuntimeException {

    public SecretResolutionException(String message) {
        super(message);
    }

    public SecretResolutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
