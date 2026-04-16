package com.jarvis.deploy.alert;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class AlertManager {
    private final Map<String, AlertRule> rules = new LinkedHashMap<>();
    private final List<AlertEvent> firedAlerts = new CopyOnWriteArrayList<>();
    private final List<AlertListener> listeners = new CopyOnWriteArrayList<>();

    public void registerRule(AlertRule rule) {
        Objects.requireNonNull(rule, "AlertRule must not be null");
        rules.put(rule.getId(), rule);
    }

    public boolean removeRule(String ruleId) {
        return rules.remove(ruleId) != null;
    }

    public Optional<AlertRule> getRule(String ruleId) {
        return Optional.ofNullable(rules.get(ruleId));
    }

    public Collection<AlertRule> getAllRules() {
        return Collections.unmodifiableCollection(rules.values());
    }

    public void addListener(AlertListener listener) {
        listeners.add(listener);
    }

    public AlertEvent fireAlert(String ruleId, String environment, String message) {
        AlertRule rule = rules.get(ruleId);
        if (rule == null) throw new IllegalArgumentException("Unknown rule: " + ruleId);
        if (!rule.isEnabled()) return null;
        AlertEvent event = new AlertEvent(rule, environment, message);
        firedAlerts.add(event);
        for (AlertListener listener : listeners) {
            listener.onAlert(event);
        }
        return event;
    }

    public List<AlertEvent> getFiredAlerts() {
        return Collections.unmodifiableList(firedAlerts);
    }

    public List<AlertEvent> getAlertsByEnvironment(String environment) {
        List<AlertEvent> result = new ArrayList<>();
        for (AlertEvent e : firedAlerts) {
            if (e.getEnvironment().equals(environment)) result.add(e);
        }
        return result;
    }

    public void clearAlerts() {
        firedAlerts.clear();
    }
}
