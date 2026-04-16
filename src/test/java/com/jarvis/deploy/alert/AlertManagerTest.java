package com.jarvis.deploy.alert;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AlertManagerTest {
    private AlertManager alertManager;
    private AlertRule criticalRule;
    private AlertRule warningRule;

    @BeforeEach
    void setUp() {
        alertManager = new AlertManager();
        criticalRule = new AlertRule("rule-1", "High Error Rate", AlertSeverity.CRITICAL, "errorRate > 0.1");
        warningRule = new AlertRule("rule-2", "Slow Response", AlertSeverity.WARNING, "responseTime > 2000");
        alertManager.registerRule(criticalRule);
        alertManager.registerRule(warningRule);
    }

    @Test
    void testRegisterAndGetRule() {
        assertTrue(alertManager.getRule("rule-1").isPresent());
        assertEquals("High Error Rate", alertManager.getRule("rule-1").get().getName());
    }

    @Test
    void testFireAlertNotifiesListeners() {
        List<AlertEvent> received = new ArrayList<>();
        alertManager.addListener(received::add);
        AlertEvent event = alertManager.fireAlert("rule-1", "production", "Error rate exceeded threshold");
        assertNotNull(event);
        assertEquals(1, received.size());
        assertEquals(AlertSeverity.CRITICAL, received.get(0).getSeverity());
    }

    @Test
    void testDisabledRuleDoesNotFire() {
        criticalRule.setEnabled(false);
        List<AlertEvent> received = new ArrayList<>();
        alertManager.addListener(received::add);
        AlertEvent event = alertManager.fireAlert("rule-1", "production", "Should not fire");
        assertNull(event);
        assertTrue(received.isEmpty());
    }

    @Test
    void testGetAlertsByEnvironment() {
        alertManager.fireAlert("rule-1", "production", "prod alert");
        alertManager.fireAlert("rule-2", "staging", "staging alert");
        alertManager.fireAlert("rule-1", "production", "another prod alert");
        assertEquals(2, alertManager.getAlertsByEnvironment("production").size());
        assertEquals(1, alertManager.getAlertsByEnvironment("staging").size());
    }

    @Test
    void testRemoveRule() {
        assertTrue(alertManager.removeRule("rule-2"));
        assertFalse(alertManager.getRule("rule-2").isPresent());
        assertFalse(alertManager.removeRule("nonexistent"));
    }

    @Test
    void testClearAlerts() {
        alertManager.fireAlert("rule-1", "production", "test");
        alertManager.clearAlerts();
        assertTrue(alertManager.getFiredAlerts().isEmpty());
    }

    @Test
    void testFireUnknownRuleThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> alertManager.fireAlert("unknown", "production", "msg"));
    }
}
