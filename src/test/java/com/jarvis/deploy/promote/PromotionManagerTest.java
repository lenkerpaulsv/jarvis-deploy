package com.jarvis.deploy.promote;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PromotionManagerTest {

    private PromotionManager manager;

    @BeforeEach
    void setUp() {
        manager = new PromotionManager();
    }

    @Test
    void createRequest_validPromotion_returnsPendingRequest() {
        PromotionRequest req = manager.createRequest("dep-1", "dev", "staging", "alice");
        assertNotNull(req);
        assertEquals(PromotionStatus.PENDING, req.getStatus());
        assertEquals("dev", req.getSourceEnvironment());
        assertEquals("staging", req.getTargetEnvironment());
    }

    @Test
    void createRequest_downgradePromotion_throwsException() {
        assertThrows(PromotionException.class,
                () -> manager.createRequest("dep-1", "staging", "dev", "alice"));
    }

    @Test
    void createRequest_duplicateActive_throwsException() {
        manager.createRequest("dep-1", "dev", "staging", "alice");
        assertThrows(PromotionException.class,
                () -> manager.createRequest("dep-1", "dev", "staging", "bob"));
    }

    @Test
    void approve_pendingRequest_setsInProgress() {
        PromotionRequest req = manager.createRequest("dep-2", "dev", "staging", "alice");
        manager.approve(req);
        assertEquals(PromotionStatus.IN_PROGRESS, req.getStatus());
    }

    @Test
    void approve_nonPendingRequest_throwsException() {
        PromotionRequest req = manager.createRequest("dep-3", "dev", "staging", "alice");
        manager.approve(req);
        assertThrows(PromotionException.class, () -> manager.approve(req));
    }

    @Test
    void complete_success_removesFromActive() {
        PromotionRequest req = manager.createRequest("dep-4", "staging", "production", "alice");
        manager.approve(req);
        manager.complete(req, true);
        assertEquals(PromotionStatus.SUCCEEDED, req.getStatus());
        assertTrue(manager.getActivePromotions().isEmpty());
    }

    @Test
    void cancel_pendingRequest_setsCancelled() {
        PromotionRequest req = manager.createRequest("dep-5", "dev", "staging", "alice");
        manager.cancel(req);
        assertEquals(PromotionStatus.CANCELLED, req.getStatus());
        assertTrue(manager.getActivePromotions().isEmpty());
    }

    @Test
    void cancel_inProgressRequest_throwsException() {
        PromotionRequest req = manager.createRequest("dep-6", "dev", "staging", "alice");
        manager.approve(req);
        assertThrows(PromotionException.class, () -> manager.cancel(req));
    }
}
