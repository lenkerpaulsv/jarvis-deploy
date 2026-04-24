package com.jarvis.deploy.trace;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentTracerTest {

    private DeploymentTracer tracer;

    @BeforeEach
    void setUp() {
        tracer = new DeploymentTracer();
    }

    @Test
    void startSpan_createsAndRegistersSpan() {
        TraceSpan span = tracer.startSpan("dep-001", "validate");

        assertNotNull(span);
        assertEquals("dep-001", span.getDeploymentId());
        assertEquals("validate", span.getOperation());
        assertFalse(span.isFinished());
        assertNotNull(span.getStartTime());
    }

    @Test
    void finishSpan_marksSpanFinishedWithMetadata() {
        TraceSpan span = tracer.startSpan("dep-002", "deploy");
        tracer.finishSpan(span, Map.of("env", "staging", "result", "success"));

        assertTrue(span.isFinished());
        assertNotNull(span.getEndTime());
        assertNotNull(span.getDuration());
        assertTrue(span.getDuration().toMillis() >= 0);
        assertEquals("staging", span.getMetadata().get("env"));
        assertEquals("success", span.getMetadata().get("result"));
    }

    @Test
    void finishSpan_twice_throwsIllegalStateException() {
        TraceSpan span = tracer.startSpan("dep-003", "cleanup");
        tracer.finishSpan(span, null);

        assertThrows(IllegalStateException.class, () -> tracer.finishSpan(span, null));
    }

    @Test
    void getSpans_returnsAllSpansForDeployment() {
        tracer.startSpan("dep-004", "preflight");
        tracer.startSpan("dep-004", "execute");
        tracer.startSpan("dep-004", "notify");

        List<TraceSpan> spans = tracer.getSpans("dep-004");
        assertEquals(3, spans.size());
    }

    @Test
    void getSpans_unknownDeployment_returnsEmptyList() {
        List<TraceSpan> spans = tracer.getSpans("nonexistent");
        assertNotNull(spans);
        assertTrue(spans.isEmpty());
    }

    @Test
    void clearTrace_removesAllSpansForDeployment() {
        tracer.startSpan("dep-005", "stage1");
        tracer.startSpan("dep-005", "stage2");
        assertEquals(1, tracer.activeTraceCount());

        tracer.clearTrace("dep-005");

        assertEquals(0, tracer.activeTraceCount());
        assertTrue(tracer.getSpans("dep-005").isEmpty());
    }

    @Test
    void startSpan_blankDeploymentId_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> tracer.startSpan("", "op"));
    }

    @Test
    void startSpan_nullOperation_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> tracer.startSpan("dep-006", null));
    }

    @Test
    void getDuration_unfinishedSpan_returnsNull() {
        TraceSpan span = tracer.startSpan("dep-007", "pending");
        assertNull(span.getDuration());
    }
}
