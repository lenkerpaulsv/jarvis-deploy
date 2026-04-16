package com.jarvis.deploy.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentEventBusTest {

    private DeploymentEventBus eventBus;

    @BeforeEach
    void setUp() {
        eventBus = new DeploymentEventBus();
    }

    @Test
    void testSubscribeAndPublish() {
        List<DeploymentEvent> received = new ArrayList<>();
        eventBus.subscribe(DeploymentEventType.DEPLOY_STARTED, received::add);

        DeploymentEvent event = new DeploymentEvent(DeploymentEventType.DEPLOY_STARTED, "app-1", "prod");
        eventBus.publish(event);

        assertEquals(1, received.size());
        assertEquals(DeploymentEventType.DEPLOY_STARTED, received.get(0).getType());
        assertEquals("app-1", received.get(0).getAppId());
    }

    @Test
    void testPublishToUnsubscribedTypeDoesNothing() {
        List<DeploymentEvent> received = new ArrayList<>();
        eventBus.subscribe(DeploymentEventType.DEPLOY_STARTED, received::add);

        eventBus.publish(new DeploymentEvent(DeploymentEventType.DEPLOY_FAILED, "app-2", "staging"));

        assertTrue(received.isEmpty());
    }

    @Test
    void testUnsubscribe() {
        List<DeploymentEvent> received = new ArrayList<>();
        DeploymentEventListener listener = received::add;
        eventBus.subscribe(DeploymentEventType.DEPLOY_COMPLETED, listener);
        eventBus.unsubscribe(DeploymentEventType.DEPLOY_COMPLETED, listener);

        eventBus.publish(new DeploymentEvent(DeploymentEventType.DEPLOY_COMPLETED, "app-3", "dev"));

        assertTrue(received.isEmpty());
    }

    @Test
    void testListenerCountAfterSubscribe() {
        eventBus.subscribe(DeploymentEventType.ROLLBACK_STARTED, e -> {});
        eventBus.subscribe(DeploymentEventType.ROLLBACK_STARTED, e -> {});
        assertEquals(2, eventBus.listenerCount(DeploymentEventType.ROLLBACK_STARTED));
    }

    @Test
    void testFaultyListenerDoesNotBreakOthers() {
        List<String> results = new ArrayList<>();
        eventBus.subscribe(DeploymentEventType.DEPLOY_STARTED, e -> { throw new RuntimeException("boom"); });
        eventBus.subscribe(DeploymentEventType.DEPLOY_STARTED, e -> results.add("ok"));

        assertDoesNotThrow(() -> eventBus.publish(new DeploymentEvent(DeploymentEventType.DEPLOY_STARTED, "app-4", "prod")));
        assertEquals(1, results.size());
    }

    @Test
    void testPublishNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> eventBus.publish(null));
    }

    @Test
    void testClearAll() {
        eventBus.subscribe(DeploymentEventType.DEPLOY_STARTED, e -> {});
        eventBus.clearAll();
        assertEquals(0, eventBus.listenerCount(DeploymentEventType.DEPLOY_STARTED));
    }
}
