package com.jarvis.deploy.notification;

import com.jarvis.deploy.deployment.DeploymentRecord;

/**
 * Interface for receiving deployment notification events.
 * Implement this to add custom notification channels (e.g., Slack, email, webhook).
 */
public interface NotificationListener {

    /**
     * Called when a deployment notification event occurs.
     *
     * @param event   the type of deployment event
     * @param message human-readable notification message
     * @param record  the associated deployment record
     */
    void onNotification(NotificationEvent event, String message, DeploymentRecord record);
}
