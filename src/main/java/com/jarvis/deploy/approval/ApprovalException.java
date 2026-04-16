package com.jarvis.deploy.approval;

/**
 * Thrown when an approval operation cannot be completed.
 */
public class ApprovalException extends Exception {

    private final String approvalId;

    public ApprovalException(String message) {
        super(message);
        this.approvalId = null;
    }

    public ApprovalException(String message, Throwable cause) {
        super(message, cause);
        this.approvalId = null;
    }

    /**
     * Creates an exception associated with a specific approval ID.
     *
     * @param approvalId the ID of the approval that caused the exception
     * @param message    a description of the error
     */
    public ApprovalException(String approvalId, String message) {
        super(message);
        this.approvalId = approvalId;
    }

    /**
     * Returns the approval ID associated with this exception, or {@code null}
     * if no approval ID was provided.
     *
     * @return the approval ID, or {@code null}
     */
    public String getApprovalId() {
        return approvalId;
    }
}
