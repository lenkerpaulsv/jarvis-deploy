package com.jarvis.deploy.approval;

/**
 * Thrown when an approval operation cannot be completed.
 */
public class ApprovalException extends Exception {

    public ApprovalException(String message) {
        super(message);
    }

    public ApprovalException(String message, Throwable cause) {
        super(message, cause);
    }
}
