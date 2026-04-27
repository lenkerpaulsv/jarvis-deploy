package com.jarvis.deploy.signal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentSignalHandlerTest {

    private DeploymentSignalHandler handler;

    @BeforeEach
    void setUp() {
        handler = new DeploymentSignalHandler();
    }

    @Test
    void testRegisterAndDispatchSignal() {
        List<DeploymentSignal> received = new ArrayList<>();
        handler.registerHandler(DeploymentSignalType.PAUSE, received::add);

        DeploymentSignal signal = new DeploymentSignal("dep-001", DeploymentSignalType.PAUSE, "admin", "maintenance");
        handler.sendSignal(signal);

        assertEquals(1, received.size());
        assertEquals("dep-001", received.get(0).getDeploymentId());
        assertEquals(DeploymentSignalType.PAUSE, received.get(0).getType());
    }

    @Test
    void testSignalClearedAfterHandling() {
        handler.registerHandler(DeploymentSignalType.ABORT, s -> {});

        DeploymentSignal signal = new DeploymentSignal("dep-002", DeploymentSignalType.ABORT, "ci", "test failure");
        handler.sendSignal(signal);

        assertFalse(handler.hasPendingSignal("dep-002"));
    }

    @Test
    void testSignalRemainsWhenNoHandlerRegistered() {
        DeploymentSignal signal = new DeploymentSignal("dep-003", DeploymentSignalType.DRAIN, "ops", "scale down");
        handler.sendSignal(signal);

        assertTrue(handler.hasPendingSignal("dep-003"));
        assertEquals(DeploymentSignalType.DRAIN, handler.getPendingSignal("dep-003").getType());
    }

    @Test
    void testClearSignalManually() {
        DeploymentSignal signal = new DeploymentSignal("dep-004", DeploymentSignalType.PAUSE, "user", "");
        handler.sendSignal(signal);
        handler.clearSignal("dep-004");

        assertFalse(handler.hasPendingSignal("dep-004"));
    }

    @Test
    void testPendingCount() {
        handler.sendSignal(new DeploymentSignal("dep-010", DeploymentSignalType.PAUSE, "admin", ""));
        handler.sendSignal(new DeploymentSignal("dep-011", DeploymentSignalType.DRAIN, "admin", ""));

        assertEquals(2, handler.pendingCount());
    }

    @Test
    void testNullSignalThrows() {
        assertThrows(IllegalArgumentException.class, () -> handler.sendSignal(null));
    }

    @Test
    void testNullHandlerRegistrationThrows() {
        assertThrows(IllegalArgumentException.class, () -> handler.registerHandler(DeploymentSignalType.ABORT, null));
    }

    @Test
    void testSignalToStringContainsFields() {
        DeploymentSignal signal = new DeploymentSignal("dep-099", DeploymentSignalType.ABORT, "system", "timeout");
        String str = signal.toString();
        assertTrue(str.contains("dep-099"));
        assertTrue(str.contains("ABORT"));
        assertTrue(str.contains("timeout"));
    }
}
