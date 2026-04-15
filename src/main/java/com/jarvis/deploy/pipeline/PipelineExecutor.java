package com.jarvis.deploy.pipeline;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * Executes a DeploymentPipeline stage by stage, handling failures and status transitions.
 */
public class PipelineExecutor {

    private static final Logger logger = Logger.getLogger(PipelineExecutor.class.getName());

    public PipelineResult execute(DeploymentPipeline pipeline) {
        Objects.requireNonNull(pipeline, "pipeline must not be null");

        if (pipeline.getStages().isEmpty()) {
            return PipelineResult.failure(pipeline.getPipelineId(), "Pipeline has no stages defined.");
        }

        pipeline.setStatus(PipelineStatus.RUNNING);
        logger.info("Starting pipeline: " + pipeline.getPipelineId() +
                    " for environment: " + pipeline.getEnvironment());

        while (pipeline.hasNextStage()) {
            PipelineStage stage = pipeline.currentStage();
            logger.info("Executing stage: " + stage.getName());

            try {
                stage.execute();
                logger.info("Stage completed: " + stage.getName());
            } catch (Exception e) {
                logger.severe("Stage failed: " + stage.getName() + " - " + e.getMessage());
                pipeline.setStatus(PipelineStatus.FAILED);
                return PipelineResult.failure(pipeline.getPipelineId(),
                        "Stage '" + stage.getName() + "' failed: " + e.getMessage());
            }

            pipeline.advanceStage();
        }

        pipeline.setStatus(PipelineStatus.COMPLETED);
        logger.info("Pipeline completed successfully: " + pipeline.getPipelineId());
        return PipelineResult.success(pipeline.getPipelineId());
    }
}
