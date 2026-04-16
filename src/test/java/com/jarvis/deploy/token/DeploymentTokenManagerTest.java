package com.jarvis.deploy.token;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentTokenManagerTest {

    private DeploymentTokenManager manager;

    @BeforeEach
    void setUp() {
        manager = new DeploymentTokenManager();
    }

    @Test
    void issueToken_shouldReturnValidToken() {
        DeploymentToken token = manager.issueToken("prod", "alice");
        assertNotNull(token);
        assertTrue(token.isValid());
        assertEquals("prod", token.getEnvironment());
        assertEquals("alice", token.getIssuedBy());
    }

    @Test
    void issueToken_shouldRejectBlankEnvironment() {
        assertThrows(IllegalArgumentException.class, () -> manager.issueToken("", "alice"));
    }

    @Test
    void issueToken_shouldRejectBlankIssuer() {
        assertThrows(IllegalArgumentException.class, () -> manager.issueToken("prod", ""));
    }

    @Test
    void validateToken_shouldReturnTrueForValidToken() {
        DeploymentToken token = manager.issueToken("staging", "bob");
        assertTrue(manager.validateToken(token.getTokenId(), "staging"));
    }

    @Test
    void validateToken_shouldReturnFalseForWrongEnvironment() {
        DeploymentToken token = manager.issueToken("staging", "bob");
        assertFalse(manager.validateToken(token.getTokenId(), "prod"));
    }

    @Test
    void revokeToken_shouldInvalidateToken() {
        DeploymentToken token = manager.issueToken("prod", "carol");
        assertTrue(manager.revokeToken(token.getTokenId()));
        assertFalse(token.isValid());
        assertFalse(manager.validateToken(token.getTokenId(), "prod"));
    }

    @Test
    void revokeToken_shouldReturnFalseForUnknownId() {
        assertFalse(manager.revokeToken("nonexistent-id"));
    }

    @Test
    void issueToken_withShortTtl_shouldExpire() throws InterruptedException {
        DeploymentToken token = manager.issueToken("dev", "dave", 1);
        assertTrue(token.isValid());
        Thread.sleep(1100);
        assertFalse(token.isValid());
        assertFalse(manager.validateToken(token.getTokenId(), "dev"));
    }

    @Test
    void purgeExpiredTokens_shouldRemoveInvalidTokens() {
        manager.issueToken("prod", "alice");
        DeploymentToken expired = manager.issueToken("dev", "bob", 1);
        try { Thread.sleep(1100); } catch (InterruptedException ignored) {}
        manager.purgeExpiredTokens();
        assertEquals(1, manager.activeTokenCount());
        assertTrue(manager.getToken(expired.getTokenId()).isEmpty());
    }
}
