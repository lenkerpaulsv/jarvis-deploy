package com.jarvis.deploy.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple synchronous event bus for broadcasting deployment lifecycle events.
 */
public class DeploymentEventBus {

    private final Map<DeploymentEventType, List<DeploymentEventListener>> listeners = new ConcurrentHashMap<>();

    public void subscribe(DeploymentEventType type, DeploymentEventListener listener) {
        if (type == null || listener == null) {
            throw new IllegalArgumentException("Event type and listener must not be null");
        }
        listeners.computeIfAbsent(type, k -> new ArrayList<>()).add(listener);
    }

    public void unsubscribe(DeploymentEventType type, DeploymentEventListener listener) {
        List<DeploymentEventListener> registered = listeners.get(type);
        if (registered != null) {
            registered.remove(listener);
        }
    }

    public void publish(DeploymentEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Event must not be null");
        }
        List<DeploymentEventListener> registered = listeners.getOrDefault(event.getType(), List.of());
        for (DeploymentEventListener listener : registered) {
            try {
                listener.onEvent(event);
            } catch (Exception e) {
                System.err.println("[EventBus] Listener error for " + event.getType() + ": " + e.getMessage());
            }
        }
    }

    public int listenerCount(DeploymentEventType type) {
        return listeners.getOrDefault(type, List.of()).size();
    }

    public void clearAll() {
        listeners.clear();
    }
}
