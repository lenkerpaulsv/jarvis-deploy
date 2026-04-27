package com.jarvis.deploy.canary;

/**
 * Lifecycle states for a canary deployment.
 */
public enum CanaryStatus {
    ACTIVE,
    PROMOTED,
    ABORTED
}
