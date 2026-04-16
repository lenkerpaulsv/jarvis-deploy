package com.jarvis.deploy.filter;

import com.jarvis.deploy.deployment.DeploymentRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeploymentFilterServiceTest {

    private DeploymentFilterService service;

    @BeforeEach
    void setUp() {
        service = new DeploymentFilterService();
    }

    private DeploymentRecord record(String env, String artifact, String status, Instant ts, List<String> tags) {
        DeploymentRecord r = mock(DeploymentRecord.class);
        when(r.getEnvironment()).thenReturn(env);
        when(r.getArtifactId()).thenReturn(artifact);
        when(r.getStatus()).thenReturn(status);
        when(r.getTimestamp()).thenReturn(ts);
        when(r.getTags()).thenReturn(tags);
        return r;
    }

    @Test
    void applyNullFilterReturnsAll() {
        List<DeploymentRecord> records = List.of(
                record("prod", "app", "SUCCESS", Instant.now(), List.of())
        );
        assertEquals(1, service.apply(records, null).size());
    }

    @Test
    void filterByEnvironment() {
        Instant now = Instant.now();
        List<DeploymentRecord> records = List.of(
                record("prod", "app", "SUCCESS", now, List.of()),
                record("staging", "app", "SUCCESS", now, List.of())
        );
        DeploymentFilter filter = DeploymentFilter.builder().environment("prod").build();
        List<DeploymentRecord> result = service.apply(records, filter);
        assertEquals(1, result.size());
    }

    @Test
    void filterByTimeRange() {
        Instant base = Instant.parse("2024-01-15T10:00:00Z");
        List<DeploymentRecord> records = List.of(
                record("prod", "app", "SUCCESS", base.minusSeconds(3600), List.of()),
                record("prod", "app", "SUCCESS", base, List.of()),
                record("prod", "app", "SUCCESS", base.plusSeconds(3600), List.of())
        );
        DeploymentFilter filter = DeploymentFilter.builder()
                .from(base.minusSeconds(1))
                .to(base.plusSeconds(1))
                .build();
        assertEquals(1, service.apply(records, filter).size());
    }

    @Test
    void filterByTag() {
        Instant now = Instant.now();
        List<DeploymentRecord> records = List.of(
                record("prod", "app", "SUCCESS", now, List.of("hotfix")),
                record("prod", "app", "SUCCESS", now, List.of("release"))
        );
        DeploymentFilter filter = DeploymentFilter.builder().tag("hotfix").build();
        assertEquals(1, service.apply(records, filter).size());
    }

    @Test
    void countAndAnyMatch() {
        Instant now = Instant.now();
        List<DeploymentRecord> records = List.of(
                record("prod", "app", "FAILED", now, List.of()),
                record("prod", "app", "SUCCESS", now, List.of())
        );
        DeploymentFilter filter = DeploymentFilter.builder().status("FAILED").build();
        assertEquals(1, service.count(records, filter));
        assertTrue(service.anyMatch(records, filter));
    }

    @Test
    void emptyRecordsReturnsEmpty() {
        DeploymentFilter filter = DeploymentFilter.builder().environment("prod").build();
        assertTrue(service.apply(List.of(), filter).isEmpty());
    }
}
