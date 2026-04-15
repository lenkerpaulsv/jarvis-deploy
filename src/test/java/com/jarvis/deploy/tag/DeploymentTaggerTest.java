package com.jarvis.deploy.tag;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentTaggerTest {

    private DeploymentTagger tagger;

    @BeforeEach
    void setUp() {
        tagger = new DeploymentTagger();
    }

    @Test
    void tagAndRetrieveSingleTag() {
        tagger.tag("deploy-001", "env", "production");
        Optional<String> value = tagger.getTag("deploy-001", "env");
        assertTrue(value.isPresent());
        assertEquals("production", value.get());
    }

    @Test
    void getTagReturnsEmptyForMissingDeployment() {
        assertTrue(tagger.getTag("nonexistent", "env").isEmpty());
    }

    @Test
    void getTagReturnsEmptyForMissingKey() {
        tagger.tag("deploy-002", "version", "1.0.0");
        assertTrue(tagger.getTag("deploy-002", "missing-key").isEmpty());
    }

    @Test
    void tagOverwritesExistingValue() {
        tagger.tag("deploy-003", "status", "pending");
        tagger.tag("deploy-003", "status", "approved");
        assertEquals("approved", tagger.getTag("deploy-003", "status").orElseThrow());
    }

    @Test
    void tagAllMergesTagsCorrectly() {
        tagger.tag("deploy-004", "existing", "value");
        tagger.tagAll("deploy-004", Map.of("ticket", "JIRA-42", "owner", "alice"));

        Map<String, String> all = tagger.getAllTags("deploy-004");
        assertEquals(3, all.size());
        assertEquals("JIRA-42", all.get("ticket"));
        assertEquals("alice", all.get("owner"));
        assertEquals("value", all.get("existing"));
    }

    @Test
    void getAllTagsReturnsEmptyMapForUnknownDeployment() {
        assertTrue(tagger.getAllTags("unknown").isEmpty());
    }

    @Test
    void getAllTagsIsUnmodifiable() {
        tagger.tag("deploy-005", "k", "v");
        Map<String, String> tags = tagger.getAllTags("deploy-005");
        assertThrows(UnsupportedOperationException.class, () -> tags.put("new", "val"));
    }

    @Test
    void removeTagReturnsTrueWhenTagExists() {
        tagger.tag("deploy-006", "removable", "yes");
        assertTrue(tagger.removeTag("deploy-006", "removable"));
        assertTrue(tagger.getTag("deploy-006", "removable").isEmpty());
    }

    @Test
    void removeTagReturnsFalseWhenTagAbsent() {
        assertFalse(tagger.removeTag("deploy-007", "ghost"));
    }

    @Test
    void clearTagsRemovesAllTagsForDeployment() {
        tagger.tagAll("deploy-008", Map.of("a", "1", "b", "2"));
        tagger.clearTags("deploy-008");
        assertTrue(tagger.getAllTags("deploy-008").isEmpty());
    }

    @Test
    void stampTimestampAddsTaggedAtKey() {
        tagger.stampTimestamp("deploy-009");
        assertTrue(tagger.getTag("deploy-009", "tagged_at").isPresent());
    }

    @Test
    void tagThrowsOnBlankKey() {
        assertThrows(IllegalArgumentException.class,
                () -> tagger.tag("deploy-010", "  ", "value"));
    }

    @Test
    void tagThrowsOnNullDeploymentId() {
        assertThrows(NullPointerException.class,
                () -> tagger.tag(null, "key", "value"));
    }
}
