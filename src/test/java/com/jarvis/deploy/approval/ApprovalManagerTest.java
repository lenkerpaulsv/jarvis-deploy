package com.jarvis.deploy.approval;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ApprovalManagerTest {

    private ApprovalManager manager;

    @BeforeEach
    void setUp() {
        manager = new ApprovalManager(Duration.ofMinutes(30));
    }

    @Test
    void createRequest_shouldReturnPendingRequest() {
        ApprovalRequest req = manager.createRequest("production", "1.2.3", "alice");
        assertNotNull(req.getRequestId());
        assertEquals("production", req.getEnvironment());
        assertEquals("1.2.3", req.getArtifactVersion());
        assertEquals("alice", req.getRequestedBy());
        assertEquals(ApprovalRequest.Status.PENDING, req.getStatus());
    }

    @Test
    void approve_shouldSetStatusApproved() throws ApprovalException {
        ApprovalRequest req = manager.createRequest("staging", "2.0.0", "bob");
        manager.approve(req.getRequestId(), "manager", "Looks good");
        assertEquals(ApprovalRequest.Status.APPROVED, req.getStatus());
        assertEquals("manager", req.getReviewedBy());
        assertEquals("Looks good", req.getComment());
    }

    @Test
    void reject_shouldSetStatusRejected() throws ApprovalException {
        ApprovalRequest req = manager.createRequest("production", "3.1.0", "charlie");
        manager.reject(req.getRequestId(), "lead", "Not ready");
        assertEquals(ApprovalRequest.Status.REJECTED, req.getStatus());
        assertEquals("lead", req.getReviewedBy());
    }

    @Test
    void approve_nonExistentId_shouldThrow() {
        assertThrows(ApprovalException.class,
                () -> manager.approve("non-existent", "reviewer", "ok"));
    }

    @Test
    void approve_alreadyApproved_shouldThrow() throws ApprovalException {
        ApprovalRequest req = manager.createRequest("dev", "1.0.0", "dave");
        manager.approve(req.getRequestId(), "lead", "ok");
        assertThrows(ApprovalException.class,
                () -> manager.approve(req.getRequestId(), "lead", "again"));
    }

    @Test
    void expirePending_withZeroTtl_shouldExpireAllPending() {
        ApprovalManager shortManager = new ApprovalManager(Duration.ofNanos(1));
        shortManager.createRequest("prod", "1.0", "user1");
        shortManager.createRequest("prod", "1.1", "user2");
        // Allow TTL to pass
        try { Thread.sleep(5); } catch (InterruptedException ignored) {}
        int expired = shortManager.expirePending();
        assertEquals(2, expired);
    }

    @Test
    void listPending_shouldReturnOnlyPendingRequests() throws ApprovalException {
        ApprovalRequest r1 = manager.createRequest("staging", "1.0", "user1");
        ApprovalRequest r2 = manager.createRequest("staging", "1.1", "user2");
        manager.approve(r1.getRequestId(), "lead", "ok");
        List<ApprovalRequest> pending = manager.listPending();
        assertEquals(1, pending.size());
        assertEquals(r2.getRequestId(), pending.get(0).getRequestId());
    }

    @Test
    void findById_shouldReturnCorrectRequest() {
        ApprovalRequest req = manager.createRequest("dev", "0.9", "tester");
        Optional<ApprovalRequest> found = manager.findById(req.getRequestId());
        assertTrue(found.isPresent());
        assertEquals(req.getRequestId(), found.get().getRequestId());
    }

    @Test
    void findById_unknownId_shouldReturnEmpty() {
        Optional<ApprovalRequest> found = manager.findById("unknown-id");
        assertFalse(found.isPresent());
    }
}
