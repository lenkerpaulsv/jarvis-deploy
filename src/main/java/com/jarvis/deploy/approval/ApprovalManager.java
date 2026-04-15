package com.jarvis.deploy.approval;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages lifecycle of deployment approval requests.
 */
public class ApprovalManager {

    private final Map<String, ApprovalRequest> requests = new ConcurrentHashMap<>();
    private final Duration defaultTtl;

    public ApprovalManager(Duration defaultTtl) {
        this.defaultTtl = Objects.requireNonNull(defaultTtl, "defaultTtl must not be null");
    }

    /**
     * Creates and registers a new approval request.
     */
    public ApprovalRequest createRequest(String environment, String artifactVersion, String requestedBy) {
        String id = UUID.randomUUID().toString();
        Instant now = Instant.now();
        ApprovalRequest request = new ApprovalRequest(id, environment, artifactVersion,
                requestedBy, now, now.plus(defaultTtl));
        requests.put(id, request);
        return request;
    }

    /**
     * Approves a pending request by its ID.
     *
     * @throws ApprovalException if the request is not found, not pending, or expired.
     */
    public void approve(String requestId, String reviewer, String comment) throws ApprovalException {
        ApprovalRequest req = getAndValidate(requestId);
        req.approve(reviewer, comment);
    }

    /**
     * Rejects a pending request by its ID.
     *
     * @throws ApprovalException if the request is not found, not pending, or expired.
     */
    public void reject(String requestId, String reviewer, String comment) throws ApprovalException {
        ApprovalRequest req = getAndValidate(requestId);
        req.reject(reviewer, comment);
    }

    /**
     * Expires all pending requests whose TTL has passed.
     *
     * @return number of requests marked as expired.
     */
    public int expirePending() {
        Instant now = Instant.now();
        int count = 0;
        for (ApprovalRequest req : requests.values()) {
            if (req.isPending() && req.isExpired(now)) {
                req.markExpired();
                count++;
            }
        }
        return count;
    }

    public Optional<ApprovalRequest> findById(String requestId) {
        return Optional.ofNullable(requests.get(requestId));
    }

    public List<ApprovalRequest> listPending() {
        List<ApprovalRequest> pending = new ArrayList<>();
        for (ApprovalRequest req : requests.values()) {
            if (req.isPending()) pending.add(req);
        }
        return Collections.unmodifiableList(pending);
    }

    private ApprovalRequest getAndValidate(String requestId) throws ApprovalException {
        ApprovalRequest req = requests.get(requestId);
        if (req == null) {
            throw new ApprovalException("Approval request not found: " + requestId);
        }
        if (!req.isPending()) {
            throw new ApprovalException("Request " + requestId + " is not in PENDING state: " + req.getStatus());
        }
        if (req.isExpired(Instant.now())) {
            req.markExpired();
            throw new ApprovalException("Request " + requestId + " has expired.");
        }
        return req;
    }
}
