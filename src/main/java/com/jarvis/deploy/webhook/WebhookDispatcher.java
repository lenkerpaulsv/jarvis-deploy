package com.jarvis.deploy.webhook;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

/**
 * Dispatches webhook deliveries to registered endpoint URLs.
 * Maintains a delivery history and supports configurable timeouts.
 */
public class WebhookDispatcher {

    private static final Logger log = Logger.getLogger(WebhookDispatcher.class.getName());
    private static final int DEFAULT_TIMEOUT_SECONDS = 10;

    private final List<String> registeredUrls = new CopyOnWriteArrayList<>();
    private final List<WebhookDelivery> deliveryHistory = new CopyOnWriteArrayList<>();
    private final HttpClient httpClient;
    private final int timeoutSeconds;

    public WebhookDispatcher() {
        this(DEFAULT_TIMEOUT_SECONDS);
    }

    public WebhookDispatcher(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                .build();
    }

    public void registerUrl(String url) {
        Objects.requireNonNull(url, "url must not be null");
        if (!registeredUrls.contains(url)) {
            registeredUrls.add(url);
            log.info("Registered webhook URL: " + url);
        }
    }

    public void unregisterUrl(String url) {
        registeredUrls.remove(url);
    }

    public List<WebhookDelivery> dispatch(String event, String payload) {
        Objects.requireNonNull(event, "event must not be null");
        Objects.requireNonNull(payload, "payload must not be null");

        List<WebhookDelivery> dispatched = new ArrayList<>();
        for (String url : registeredUrls) {
            WebhookDelivery delivery = new WebhookDelivery(url, event, payload);
            send(delivery);
            deliveryHistory.add(delivery);
            dispatched.add(delivery);
        }
        return Collections.unmodifiableList(dispatched);
    }

    private void send(WebhookDelivery delivery) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(delivery.getTargetUrl()))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .header("Content-Type", "application/json")
                    .header("X-Jarvis-Event", delivery.getEvent())
                    .header("X-Delivery-Id", delivery.getDeliveryId())
                    .POST(HttpRequest.BodyPublishers.ofString(delivery.getPayload()))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                delivery.markSuccess(response.statusCode());
                log.info("Webhook delivered: " + delivery);
            } else {
                delivery.markFailed(response.statusCode(), "Non-2xx response: " + response.statusCode());
                log.warning("Webhook failed: " + delivery);
            }
        } catch (Exception e) {
            delivery.markFailed(0, e.getMessage());
            log.warning("Webhook error for " + delivery.getTargetUrl() + ": " + e.getMessage());
        }
    }

    public List<WebhookDelivery> getDeliveryHistory() {
        return Collections.unmodifiableList(deliveryHistory);
    }

    public List<String> getRegisteredUrls() {
        return Collections.unmodifiableList(registeredUrls);
    }

    public void clearHistory() {
        deliveryHistory.clear();
    }
}
