package com.jarvis.deploy.pipeline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PipelineExecutorTest {

    private PipelineExecutor executor;

    @BeforeEach
    void setUp() {
        executor = new PipelineExecutor();
    }

    @Test
    void execute_successfulPipeline_returnsSuccess() {
        DeploymentPipeline pipeline = new DeploymentPipeline("p-001", "staging");
        pipeline.addStage(new PipelineStage("build", () -> {}));
        pipeline.addStage(new PipelineStage("test", () -> {}));
        pipeline.addStage(new PipelineStage("deploy", () -> {}));

        PipelineResult result = executor.execute(pipeline);

        assertTrue(result.isSuccess());
        assertEquals("p-001", result.getPipelineId());
        assertEquals(PipelineStatus.COMPLETED, pipeline.getStatus());
    }

    @Test
    void execute_stageThrowsException_returnsFailure() {
        DeploymentPipeline pipeline = new DeploymentPipeline("p-002", "production");
        pipeline.addStage(new PipelineStage("build", () -> {}));
        pipeline.addStage(new PipelineStage("test", () -> { throw new RuntimeException("Test failed"); }));
        pipeline.addStage(new PipelineStage("deploy", () -> {}));

        PipelineResult result = executor.execute(pipeline);

        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("test"));
        assertEquals(PipelineStatus.FAILED, pipeline.getStatus());
    }

    @Test
    void execute_emptyPipeline_returnsFailure() {
        DeploymentPipeline pipeline = new DeploymentPipeline("p-003", "dev");

        PipelineResult result = executor.execute(pipeline);

        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("no stages"));
    }

    @Test
    void execute_nullPipeline_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> executor.execute(null));
    }

    @Test
    void execute_firstStageFails_remainingStagesNotExecuted() {
        boolean[] secondStageRan = {false};
        DeploymentPipeline pipeline = new DeploymentPipeline("p-004", "staging");
        pipeline.addStage(new PipelineStage("build", () -> { throw new RuntimeException("Build error"); }));
        pipeline.addStage(new PipelineStage("deploy", () -> secondStageRan[0] = true));

        executor.execute(pipeline);

        assertFalse(secondStageRan[0]);
    }

    @Test
    void addStage_afterPipelineStarted_throwsIllegalStateException() {
        DeploymentPipeline pipeline = new DeploymentPipeline("p-005", "dev");
        pipeline.addStage(new PipelineStage("build", () -> {}));
        executor.execute(pipeline);

        assertThrows(IllegalStateException.class,
                () -> pipeline.addStage(new PipelineStage("extra", () -> {})));
    }
}
