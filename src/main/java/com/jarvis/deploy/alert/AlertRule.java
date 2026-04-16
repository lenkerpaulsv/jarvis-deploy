package com.jarvis.deploy.alert;

import java.util.Objects;

public class AlertRule {
    private final String id;
    private final String name;
    private final AlertSeverity severity;
    private final String condition;
    private boolean enabled;

    public AlertRule(String id, String name, AlertSeverity severity, String condition) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.severity = Objects.requireNonNull(severity);
        this.condition = Objects.requireNonNull(condition);
        this.enabled = true;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public AlertSeverity getSeverity() { return severity; }
    public String getCondition() { return condition; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    @Override
    public String toString() {
        return "AlertRule{id='" + id + "', name='" + name + "', severity=" + severity + ", enabled=" + enabled + "}";
    }
}
