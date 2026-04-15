package com.jarvis.deploy.secret;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SecretManagerTest {

    private SecretStore store;
    private SecretManager manager;

    @BeforeEach
    void setUp() {
        store = new SecretStore();
        manager = new SecretManager(store);
    }

    @Test
    void registerAndResolveSecret() {
        manager.registerSecret("prod", "DB_PASS", "s3cr3t");
        Optional<String> result = manager.resolveSecret("prod", "DB_PASS");
        assertTrue(result.isPresent());
        assertEquals("s3cr3t", result.get());
    }

    @Test
    void resolveSecret_missingKey_returnsEmpty() {
        Optional<String> result = manager.resolveSecret("prod", "MISSING_KEY");
        assertFalse(result.isPresent());
    }

    @Test
    void resolvePlaceholders_replacesKnownSecret() {
        manager.registerSecret("staging", "API_TOKEN", "tok-abc123");
        String input = "Authorization: Bearer ${secret:API_TOKEN}";
        String resolved = manager.resolvePlaceholders("staging", input);
        assertEquals("Authorization: Bearer tok-abc123", resolved);
    }

    @Test
    void resolvePlaceholders_multiplePlaceholders() {
        manager.registerSecret("dev", "USER", "admin");
        manager.registerSecret("dev", "PASS", "pass123");
        String input = "jdbc:mysql://localhost/${secret:USER}:${secret:PASS}@mydb";
        String resolved = manager.resolvePlaceholders("dev", input);
        assertEquals("jdbc:mysql://localhost/admin:pass123@mydb", resolved);
    }

    @Test
    void resolvePlaceholders_unknownSecret_throwsException() {
        String input = "value=${secret:UNKNOWN}";
        assertThrows(SecretResolutionException.class,
            () -> manager.resolvePlaceholders("prod", input));
    }

    @Test
    void resolvePlaceholders_noPlaceholders_returnsOriginal() {
        String input = "plain-value";
        assertEquals(input, manager.resolvePlaceholders("prod", input));
    }

    @Test
    void revokeSecret_removesEntry() {
        manager.registerSecret("prod", "TOKEN", "abc");
        assertTrue(manager.revokeSecret("prod", "TOKEN"));
        assertFalse(manager.resolveSecret("prod", "TOKEN").isPresent());
    }

    @Test
    void revokeSecret_nonExistent_returnsFalse() {
        assertFalse(manager.revokeSecret("prod", "GHOST_KEY"));
    }

    @Test
    void listSecrets_returnsAllForEnvironment() {
        manager.registerSecret("qa", "KEY1", "v1");
        manager.registerSecret("qa", "KEY2", "v2");
        manager.registerSecret("prod", "KEY1", "v3");
        Map<String, SecretEntry> qaSecrets = manager.listSecrets("qa");
        assertEquals(2, qaSecrets.size());
        assertTrue(qaSecrets.containsKey("KEY1"));
        assertTrue(qaSecrets.containsKey("KEY2"));
    }

    @Test
    void constructorRejectsNullStore() {
        assertThrows(NullPointerException.class, () -> new SecretManager(null));
    }
}
