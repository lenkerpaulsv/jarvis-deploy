package com.jarvis.deploy.comparison;

import com.jarvis.deploy.deployment.DeploymentRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link DeploymentComparator}.
 * Verifies field-level diff detection, summary generation, and edge cases.
 */
class DeploymentComparatorTest {

    private DeploymentComparator comparator;

    private DeploymentRecord baseRecord;
    private DeploymentRecord updatedRecord;

    @BeforeEach
    void setUp() {
        comparator = new DeploymentComparator();

        baseRecord = new DeploymentRecord(
                "deploy-001",
                "my-service",
                "1.0.0",
                "production",
                "SUCCESS",
                Instant.parse("2024-06-01T10:00:00Z"),
                Map.of("replicas", "3", "memory", "512m", "timeout", "30s")
        );

        updatedRecord = new DeploymentRecord(
                "deploy-002",
                "my-service",
                "1.1.0",
                "production",
                "SUCCESS",
                Instant.parse("2024-06-15T14:30:00Z"),
                Map.of("replicas", "5", "memory", "512m", "timeout", "60s", "debug", "true")
        );
    }

    @Test
    void compare_detectsVersionChange() {
        List<DeploymentComparator.FieldDiff> diffs = comparator.compare(baseRecord, updatedRecord);

        assertTrue(diffs.stream().anyMatch(d ->
                d.getField().equals("version")
                        && d.getOldValue().equals("1.0.0")
                        && d.getNewValue().equals("1.1.0")
        ), "Should detect version change from 1.0.0 to 1.1.0");
    }

    @Test
    void compare_detectsConfigChanges() {
        List<DeploymentComparator.FieldDiff> diffs = comparator.compare(baseRecord, updatedRecord);

        assertTrue(diffs.stream().anyMatch(d ->
                d.getField().contains("replicas") && d.getOldValue().equals("3") && d.getNewValue().equals("5")
        ), "Should detect replicas config change");

        assertTrue(diffs.stream().anyMatch(d ->
                d.getField().contains("timeout") && d.getOldValue().equals("30s") && d.getNewValue().equals("60s")
        ), "Should detect timeout config change");
    }

    @Test
    void compare_detectsAddedConfigKey() {
        List<DeploymentComparator.FieldDiff> diffs = comparator.compare(baseRecord, updatedRecord);

        assertTrue(diffs.stream().anyMatch(d ->
                d.getField().contains("debug") && d.getOldValue() == null && d.getNewValue().equals("true")
        ), "Should detect newly added config key 'debug'");
    }

    @Test
    void compare_unchangedFieldsNotReported() {
        List<DeploymentComparator.FieldDiff> diffs = comparator.compare(baseRecord, updatedRecord);

        assertFalse(diffs.stream().anyMatch(d ->
                d.getField().contains("memory")
        ), "Unchanged 'memory' config should not appear in diffs");

        assertFalse(diffs.stream().anyMatch(d ->
                d.getField().equals("environment")
        ), "Unchanged 'environment' field should not appear in diffs");
    }

    @Test
    void compare_identicalRecordsProduceNoDiffs() {
        List<DeploymentComparator.FieldDiff> diffs = comparator.compare(baseRecord, baseRecord);
        assertTrue(diffs.isEmpty(), "Comparing a record with itself should produce no diffs");
    }

    @Test
    void compare_nullOldRecordThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> comparator.compare(null, updatedRecord));
    }

    @Test
    void compare_nullNewRecordThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> comparator.compare(baseRecord, null));
    }

    @Test
    void summarize_returnsHumanReadableSummary() {
        List<DeploymentComparator.FieldDiff> diffs = comparator.compare(baseRecord, updatedRecord);
        String summary = comparator.summarize(diffs);

        assertNotNull(summary);
        assertFalse(summary.isBlank(), "Summary should not be blank when diffs exist");
        assertTrue(summary.contains("version") || summary.contains("replicas"),
                "Summary should mention at least one changed field");
    }

    @Test
    void summarize_emptyDiffsReturnsNoChangesMessage() {
        String summary = comparator.summarize(List.of());
        assertNotNull(summary);
        assertTrue(summary.toLowerCase().contains("no change") || summary.toLowerCase().contains("identical"),
                "Summary of empty diffs should indicate no changes");
    }

    @Test
    void compare_removedConfigKeyDetected() {
        // base has 'timeout', updated does not
        DeploymentRecord withoutTimeout = new DeploymentRecord(
                "deploy-003",
                "my-service",
                "1.0.0",
                "production",
                "SUCCESS",
                Instant.parse("2024-06-20T09:00:00Z"),
                Map.of("replicas", "3", "memory", "512m")
        );

        List<DeploymentComparator.FieldDiff> diffs = comparator.compare(baseRecord, withoutTimeout);

        assertTrue(diffs.stream().anyMatch(d ->
                d.getField().contains("timeout") && d.getOldValue().equals("30s") && d.getNewValue() == null
        ), "Should detect removed config key 'timeout'");
    }
}
