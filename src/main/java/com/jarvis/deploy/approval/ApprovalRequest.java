package com.jarvis.deploy.approval;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a deployment approval request for a given environment and version.
 */
public class ApprovalRequest {

    public enum Status { PENDING, APPROVED, REJECTED, EXPIRED }

    private final String requestId;
    private final String environment;
    private final String artifactVersion;
    private final String requestedBy;
    private final Instant createdAt;
    private final Instant expiresAt;
    private Status status;
    private String reviewedBy;
    private String comment;

    public ApprovalRequest(String requestId, String environment, String artifactVersion,
                           String requestedBy, Instant createdAt, Instant expiresAt) {
        this.requestId = Objects.requireNonNull(requestId, "requestId must not be null");
        this.environment = Objects.requireNonNull(environment, "environment must not be null");
        this.artifactVersion = Objects.requireNonNull(artifactVersion, "artifactVersion must not be null");
        this.requestedBy = Objects.requireNonNull(requestedBy, "requestedBy must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt must not be null");
        this.status = Status.PENDING;
    }

    public String getRequestId()       { return requestId; }
    public String getEnvironment()     { return environment; }
    public String getArtifactVersion() { return artifactVersion; }
    public String getRequestedBy()     { return requestedBy; }
    public Instant getCreatedAt()      { return createdAt; }
    public Instant getExpiresAt()      { return expiresAt; }
    public Status getStatus()          { return status; }
    public String getReviewedBy()      { return reviewedBy; }
    public String getComment()         { return comment; }

    public void approve(String reviewer, String comment) {
        this.status = Status.APPROVED;
        this.reviewedBy = reviewer;
        this.comment = comment;
    }

    public void reject(String reviewer, String comment) {
        this.status = Status.REJECTED;
        this.reviewedBy = reviewer;
        this.comment = comment;
    }

    public void markExpired() {
        if (this.status == Status.PENDING) {
            this.status = Status.EXPIRED;
        }
    }

    public boolean isPending()  { return status == Status.PENDING; }
    public boolean isApproved() { return status == Status.APPROVED; }
    public boolean isExpired(Instant now) { return now.isAfter(expiresAt); }

    @Override
    public String toString() {
        return String.format("ApprovalRequest{id='%s', env='%s', version='%s', status=%s}",
                requestId, environment, artifactVersion, status);
    }
}
