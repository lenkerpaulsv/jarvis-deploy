package com.jarvis.deploy.alert;

@FunctionalInterface
public interface AlertListener {
    void onAlert(AlertEvent event);
}
