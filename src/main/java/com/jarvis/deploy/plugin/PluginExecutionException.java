package com.jarvis.deploy.plugin;

/**
 * Thrown when a deployment plugin encounters an error during execution.
 */
public class PluginExecutionException extends Exception {

    private final String pluginName;

    public PluginExecutionException(String pluginName, String message) {
        super("[" + pluginName + "] " + message);
        this.pluginName = pluginName;
    }

    public PluginExecutionException(String pluginName, String message, Throwable cause) {
        super("[" + pluginName + "] " + message, cause);
        this.pluginName = pluginName;
    }

    public String getPluginName() {
        return pluginName;
    }
}
