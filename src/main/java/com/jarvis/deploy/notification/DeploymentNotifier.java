package com.jarvis.deploy.notification;

import com.jarvis.deploy.deployment.DeploymentRecord;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Handles notifications for deployment lifecycle events.
 * Supports multiple notification channels via registered listeners.
 */
public class DeploymentNotifier {

    private static final Logger logger = Logger.getLogger(DeploymentNotifier.class.getName());
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final List<NotificationListener> listeners = new ArrayList<>();

    public void addListener(NotificationListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removeListener(NotificationListener listener) {
        listeners.remove(listener);
    }

    public void notifyDeploymentStarted(DeploymentRecord record) {
        String message = String.format("[DEPLOY STARTED] env=%s version=%s at %s",
                record.getEnvironment(),
                record.getVersion(),
                record.getTimestamp().format(FORMATTER));
        dispatch(NotificationEvent.STARTED, message, record);
    }

    public void notifyDeploymentSucceeded(DeploymentRecord record) {
        String message = String.format("[DEPLOY SUCCESS] env=%s version=%s at %s",
                record.getEnvironment(),
                record.getVersion(),
                record.getTimestamp().format(FORMATTER));
        dispatch(NotificationEvent.SUCCEEDED, message, record);
    }

    public void notifyDeploymentFailed(DeploymentRecord record, String reason) {
        String message = String.format("[DEPLOY FAILED] env=%s version=%s reason='%s' at %s",
                record.getEnvironment(),
                record.getVersion(),
                reason,
                record.getTimestamp().format(FORMATTER));
        dispatch(NotificationEvent.FAILED, message, record);
    }

    public void notifyRollback(DeploymentRecord record) {
        String message = String.format("[ROLLBACK] env=%s rolled back to version=%s at %s",
                record.getEnvironment(),
                record.getVersion(),
                record.getTimestamp().format(FORMATTER));
        dispatch(NotificationEvent.ROLLBACK, message, record);
    }

    private void dispatch(NotificationEvent event, String message, DeploymentRecord record) {
        logger.info(message);
        for (NotificationListener listener : listeners) {
            try {
                listener.onNotification(event, message, record);
            } catch (Exception e) {
                logger.warning("Notification listener failed: " + e.getMessage());
            }
        }
    }
}
