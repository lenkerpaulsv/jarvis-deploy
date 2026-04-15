package com.jarvis.deploy.notification;

/**
 * Enum representing the types of deployment notification events.
 */
public enum NotificationEvent {

    /** Fired when a deployment has been initiated. */
    STARTED,

    /** Fired when a deployment completes successfully. */
    SUCCEEDED,

    /** Fired when a deployment fails. */
    FAILED,

    /** Fired when a rollback is triggered. */
    ROLLBACK
}
