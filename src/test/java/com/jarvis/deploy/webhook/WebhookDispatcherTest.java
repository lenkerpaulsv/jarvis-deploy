package com.jarvis.deploy.webhook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WebhookDispatcherTest {

    private WebhookDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        dispatcher = new WebhookDispatcher(5);
    }

    @Test
    void registerUrl_addsUrlToList() {
        dispatcher.registerUrl("http://example.com/hook");
        assertTrue(dispatcher.getRegisteredUrls().contains("http://example.com/hook"));
    }

    @Test
    void registerUrl_duplicateIsIgnored() {
        dispatcher.registerUrl("http://example.com/hook");
        dispatcher.registerUrl("http://example.com/hook");
        assertEquals(1, dispatcher.getRegisteredUrls().size());
    }

    @Test
    void unregisterUrl_removesUrl() {
        dispatcher.registerUrl("http://example.com/hook");
        dispatcher.unregisterUrl("http://example.com/hook");
        assertFalse(dispatcher.getRegisteredUrls().contains("http://example.com/hook"));
    }

    @Test
    void dispatch_withNoUrls_returnsEmptyList() {
        List<WebhookDelivery> result = dispatcher.dispatch("deployment.started", "{}");
        assertTrue(result.isEmpty());
    }

    @Test
    void dispatch_withUnreachableUrl_recordsFailedDelivery() {
        dispatcher.registerUrl("http://localhost:19999/nonexistent");
        List<WebhookDelivery> deliveries = dispatcher.dispatch("deployment.started", "{\"env\":\"prod\"}");

        assertEquals(1, deliveries.size());
        WebhookDelivery delivery = deliveries.get(0);
        assertEquals(WebhookDelivery.Status.FAILED, delivery.getStatus());
        assertEquals("deployment.started", delivery.getEvent());
        assertEquals(1, delivery.getAttemptCount());
        assertNotNull(delivery.getErrorMessage());
    }

    @Test
    void dispatch_addsToHistory() {
        dispatcher.registerUrl("http://localhost:19999/hook");
        dispatcher.dispatch("deployment.done", "{}");

        assertEquals(1, dispatcher.getDeliveryHistory().size());
    }

    @Test
    void clearHistory_removesAllDeliveries() {
        dispatcher.registerUrl("http://localhost:19999/hook");
        dispatcher.dispatch("deployment.done", "{}");
        dispatcher.clearHistory();

        assertTrue(dispatcher.getDeliveryHistory().isEmpty());
    }

    @Test
    void webhookDelivery_initialState_isPending() {
        WebhookDelivery delivery = new WebhookDelivery("http://example.com", "test.event", "{}");
        assertEquals(WebhookDelivery.Status.PENDING, delivery.getStatus());
        assertEquals(0, delivery.getAttemptCount());
        assertNotNull(delivery.getDeliveryId());
    }

    @Test
    void webhookDelivery_markSuccess_updatesState() {
        WebhookDelivery delivery = new WebhookDelivery("http://example.com", "test.event", "{}");
        delivery.markSuccess(200);
        assertEquals(WebhookDelivery.Status.SUCCESS, delivery.getStatus());
        assertEquals(200, delivery.getResponseCode());
        assertEquals(1, delivery.getAttemptCount());
        assertNull(delivery.getErrorMessage());
    }

    @Test
    void webhookDelivery_markFailed_updatesState() {
        WebhookDelivery delivery = new WebhookDelivery("http://example.com", "test.event", "{}");
        delivery.markFailed(503, "Service Unavailable");
        assertEquals(WebhookDelivery.Status.FAILED, delivery.getStatus());
        assertEquals(503, delivery.getResponseCode());
        assertEquals("Service Unavailable", delivery.getErrorMessage());
        assertEquals(1, delivery.getAttemptCount());
    }

    @Test
    void registerUrl_nullThrows() {
        assertThrows(NullPointerException.class, () -> dispatcher.registerUrl(null));
    }

    @Test
    void dispatch_nullEventThrows() {
        assertThrows(NullPointerException.class, () -> dispatcher.dispatch(null, "{}"));
    }
}
