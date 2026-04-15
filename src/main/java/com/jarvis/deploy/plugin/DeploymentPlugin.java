package com.jarvis.deploy.plugin;

/**
 * Interface for deployment lifecycle plugins.
 * Plugins can hook into pre-deploy, post-deploy, and failure events.
 */
public interface DeploymentPlugin {

    /**
     * Returns the unique name of this plugin.
     */
    String getName();

    /**
     * Called before a deployment begins.
     *
     * @param context the current plugin context
     * @throws PluginExecutionException if the pre-deploy hook fails
     */
    void beforeDeploy(PluginContext context) throws PluginExecutionException;

    /**
     * Called after a successful deployment.
     *
     * @param context the current plugin context
     * @throws PluginExecutionException if the post-deploy hook fails
     */
    void afterDeploy(PluginContext context) throws PluginExecutionException;

    /**
     * Called when a deployment fails.
     *
     * @param context   the current plugin context
     * @param cause     the exception that caused the failure
     */
    void onFailure(PluginContext context, Exception cause);
}
