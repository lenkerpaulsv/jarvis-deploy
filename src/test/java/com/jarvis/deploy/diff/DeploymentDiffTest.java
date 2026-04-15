package com.jarvis.deploy.diff;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentDiffTest {

    private Map<String, String> props(String... pairs) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            map.put(pairs[i], pairs[i + 1]);
        }
        return map;
    }

    @Test
    void testNoDiff() {
        Map<String, String> old = props("key1", "val1", "key2", "val2");
        Map<String, String> newP = props("key1", "val1", "key2", "val2");
        DeploymentDiff diff = DeploymentDiff.compute("v1", "v2", "staging", old, newP);
        assertTrue(diff.isEmpty());
    }

    @Test
    void testAddedProperties() {
        Map<String, String> old = props("key1", "val1");
        Map<String, String> newP = props("key1", "val1", "key2", "val2");
        DeploymentDiff diff = DeploymentDiff.compute("v1", "v2", "prod", old, newP);
        assertFalse(diff.isEmpty());
        assertEquals(1, diff.getAdded().size());
        assertEquals("val2", diff.getAdded().get("key2"));
        assertTrue(diff.getRemoved().isEmpty());
        assertTrue(diff.getChanged().isEmpty());
    }

    @Test
    void testRemovedProperties() {
        Map<String, String> old = props("key1", "val1", "key2", "val2");
        Map<String, String> newP = props("key1", "val1");
        DeploymentDiff diff = DeploymentDiff.compute("v2", "v3", "dev", old, newP);
        assertEquals(1, diff.getRemoved().size());
        assertEquals("val2", diff.getRemoved().get("key2"));
        assertTrue(diff.getAdded().isEmpty());
    }

    @Test
    void testChangedProperties() {
        Map<String, String> old = props("key1", "oldVal");
        Map<String, String> newP = props("key1", "newVal");
        DeploymentDiff diff = DeploymentDiff.compute("v3", "v4", "staging", old, newP);
        assertEquals(1, diff.getChanged().size());
        assertArrayEquals(new String[]{"oldVal", "newVal"}, diff.getChanged().get("key1"));
    }

    @Test
    void testMixedChanges() {
        Map<String, String> old = props("a", "1", "b", "2", "c", "3");
        Map<String, String> newP = props("a", "1", "b", "updated", "d", "4");
        DeploymentDiff diff = DeploymentDiff.compute("v1", "v2", "prod", old, newP);
        assertEquals(1, diff.getAdded().size());    // d
        assertEquals(1, diff.getRemoved().size());  // c
        assertEquals(1, diff.getChanged().size());  // b
        assertFalse(diff.isEmpty());
    }

    @Test
    void testMetadata() {
        DeploymentDiff diff = DeploymentDiff.compute("v1", "v2", "prod",
                new HashMap<>(), new HashMap<>());
        assertEquals("v1", diff.getFromVersion());
        assertEquals("v2", diff.getToVersion());
        assertEquals("prod", diff.getEnvironment());
    }

    @Test
    void testToString() {
        Map<String, String> old = props("x", "1");
        Map<String, String> newP = props("y", "2");
        DeploymentDiff diff = DeploymentDiff.compute("v1", "v2", "staging", old, newP);
        String str = diff.toString();
        assertTrue(str.contains("v1"));
        assertTrue(str.contains("v2"));
        assertTrue(str.contains("staging"));
    }

    @Test
    void testNullVersionThrows() {
        assertThrows(NullPointerException.class,
                () -> new DeploymentDiff(null, "v2", "prod"));
        assertThrows(NullPointerException.class,
                () -> new DeploymentDiff("v1", null, "prod"));
        assertThrows(NullPointerException.class,
                () -> new DeploymentDiff("v1", "v2", null));
    }
}
