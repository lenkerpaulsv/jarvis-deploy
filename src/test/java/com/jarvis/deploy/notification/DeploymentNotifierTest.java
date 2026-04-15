package com.jarvis.deploy.notification;

import com.jarvis.deploy.deployment.DeploymentRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentNotifierTest {

    private DeploymentNotifier notifier;
    private CapturingListener listener;

    @BeforeEach
    void setUp() {
        notifier = new DeploymentNotifier();
        listener = new CapturingListener();
        notifier.addListener(listener);
    }

    private DeploymentRecord makeRecord(String env, String version) {
        return new DeploymentRecord(env, version, LocalDateTime.now());
    }

    @Test
    void notifyDeploymentStarted_dispatchesStartedEvent() {
        DeploymentRecord record = makeRecord("staging", "1.0.0");
        notifier.notifyDeploymentStarted(record);

        assertEquals(1, listener.events.size());
        assertEquals(NotificationEvent.STARTED, listener.events.get(0));
        assertTrue(listener.messages.get(0).contains("DEPLOY STARTED"));
        assertTrue(listener.messages.get(0).contains("staging"));
    }

    @Test
    void notifyDeploymentSucceeded_dispatchesSucceededEvent() {
        DeploymentRecord record = makeRecord("production", "2.1.0");
        notifier.notifyDeploymentSucceeded(record);

        assertEquals(NotificationEvent.SUCCEEDED, listener.events.get(0));
        assertTrue(listener.messages.get(0).contains("DEPLOY SUCCESS"));
    }

    @Test
    void notifyDeploymentFailed_includesReason() {
        DeploymentRecord record = makeRecord("staging", "1.2.0");
        notifier.notifyDeploymentFailed(record, "health check timeout");

        assertEquals(NotificationEvent.FAILED, listener.events.get(0));
        assertTrue(listener.messages.get(0).contains("health check timeout"));
    }

    @Test
    void notifyRollback_dispatchesRollbackEvent() {
        DeploymentRecord record = makeRecord("production", "1.9.0");
        notifier.notifyRollback(record);

        assertEquals(NotificationEvent.ROLLBACK, listener.events.get(0));
        assertTrue(listener.messages.get(0).contains("ROLLBACK"));
    }

    @Test
    void removedListener_doesNotReceiveNotifications() {
        notifier.removeListener(listener);
        notifier.notifyDeploymentStarted(makeRecord("dev", "0.1.0"));
        assertTrue(listener.events.isEmpty());
    }

    @Test
    void faultyListener_doesNotPreventOtherListeners() {
        notifier.addListener((event, message, record) -> {
            throw new RuntimeException("simulated failure");
        });
        CapturingListener second = new CapturingListener();
        notifier.addListener(second);

        assertDoesNotThrow(() -> notifier.notifyDeploymentStarted(makeRecord("dev", "1.0.0")));
        assertEquals(1, second.events.size());
    }

    /** Test helper that captures dispatched events. */
    static class CapturingListener implements NotificationListener {
        final List<NotificationEvent> events = new ArrayList<>();
        final List<String> messages = new ArrayList<>();

        @Override
        public void onNotification(NotificationEvent event, String message, DeploymentRecord record) {
            events.add(event);
            messages.add(message);
        }
    }
}
