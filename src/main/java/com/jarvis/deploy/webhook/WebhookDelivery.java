package com.jarvis.deploy.webhook;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a single webhook delivery attempt with its payload and status.
 */
public class WebhookDelivery {

    public enum Status { PENDING, SUCCESS, FAILED }

    private final String deliveryId;
    private final String targetUrl;
    private final String event;
    private final String payload;
    private final Instant createdAt;
    private Status status;
    private int responseCode;
    private String errorMessage;
    private int attemptCount;

    public WebhookDelivery(String targetUrl, String event, String payload) {
        this.deliveryId = UUID.randomUUID().toString();
        this.targetUrl = Objects.requireNonNull(targetUrl, "targetUrl must not be null");
        this.event = Objects.requireNonNull(event, "event must not be null");
        this.payload = Objects.requireNonNull(payload, "payload must not be null");
        this.createdAt = Instant.now();
        this.status = Status.PENDING;
        this.attemptCount = 0;
    }

    public String getDeliveryId()   { return deliveryId; }
    public String getTargetUrl()    { return targetUrl; }
    public String getEvent()        { return event; }
    public String getPayload()      { return payload; }
    public Instant getCreatedAt()   { return createdAt; }
    public Status getStatus()       { return status; }
    public int getResponseCode()    { return responseCode; }
    public String getErrorMessage() { return errorMessage; }
    public int getAttemptCount()    { return attemptCount; }

    public void markSuccess(int responseCode) {
        this.status = Status.SUCCESS;
        this.responseCode = responseCode;
        this.errorMessage = null;
        this.attemptCount++;
    }

    public void markFailed(int responseCode, String errorMessage) {
        this.status = Status.FAILED;
        this.responseCode = responseCode;
        this.errorMessage = errorMessage;
        this.attemptCount++;
    }

    public void incrementAttempt() {
        this.attemptCount++;
    }

    @Override
    public String toString() {
        return String.format("WebhookDelivery{id='%s', event='%s', status=%s, attempts=%d}",
                deliveryId, event, status, attemptCount);
    }
}
