package com.jarvis.deploy.resume;

import com.jarvis.deploy.checkpoint.CheckpointManager;
import com.jarvis.deploy.checkpoint.DeploymentCheckpoint;
import com.jarvis.deploy.deployment.DeploymentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeploymentResumeManagerTest {

    @Mock
    private CheckpointManager checkpointManager;

    @Mock
    private DeploymentCheckpoint checkpoint;

    private DeploymentResumeManager resumeManager;

    @BeforeEach
    void setUp() {
        resumeManager = new DeploymentResumeManager(checkpointManager);
    }

    @Test
    void constructor_nullCheckpointManager_throwsIllegalArgument() {
        assertThatThrownBy(() -> new DeploymentResumeManager(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void resume_noCheckpointAvailable_throwsDeploymentException() {
        when(checkpointManager.getLatestCheckpoint("dep-1")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> resumeManager.resume("dep-1"))
                .isInstanceOf(DeploymentException.class)
                .hasMessageContaining("No checkpoint found");
    }

    @Test
    void resume_checkpointAvailable_returnsCheckpoint() throws DeploymentException {
        when(checkpointManager.getLatestCheckpoint("dep-2")).thenReturn(Optional.of(checkpoint));
        DeploymentCheckpoint result = resumeManager.resume("dep-2");
        assertThat(result).isSameAs(checkpoint);
    }

    @Test
    void resume_incrementsAttemptCount() throws DeploymentException {
        when(checkpointManager.getLatestCheckpoint("dep-3")).thenReturn(Optional.of(checkpoint));
        resumeManager.resume("dep-3");
        resumeManager.resume("dep-3");
        Optional<ResumeRecord> record = resumeManager.getResumeRecord("dep-3");
        assertThat(record).isPresent();
        assertThat(record.get().getAttempts()).isEqualTo(2);
    }

    @Test
    void resume_exceedsMaxAttempts_throwsDeploymentException() throws DeploymentException {
        when(checkpointManager.getLatestCheckpoint("dep-4")).thenReturn(Optional.of(checkpoint));
        resumeManager.resume("dep-4");
        resumeManager.resume("dep-4");
        resumeManager.resume("dep-4");
        assertThatThrownBy(() -> resumeManager.resume("dep-4"))
                .isInstanceOf(DeploymentException.class)
                .hasMessageContaining("Max resume attempts");
    }

    @Test
    void markResumed_clearsResumeRecord() throws DeploymentException {
        when(checkpointManager.getLatestCheckpoint("dep-5")).thenReturn(Optional.of(checkpoint));
        resumeManager.resume("dep-5");
        resumeManager.markResumed("dep-5");
        assertThat(resumeManager.getResumeRecord("dep-5")).isEmpty();
    }

    @Test
    void isResumable_noCheckpoint_returnsFalse() {
        when(checkpointManager.getLatestCheckpoint("dep-6")).thenReturn(Optional.empty());
        assertThat(resumeManager.isResumable("dep-6")).isFalse();
    }

    @Test
    void isResumable_checkpointPresentWithinLimit_returnsTrue() {
        when(checkpointManager.getLatestCheckpoint("dep-7")).thenReturn(Optional.of(checkpoint));
        assertThat(resumeManager.isResumable("dep-7")).isTrue();
    }

    @Test
    void resume_blankDeploymentId_throwsIllegalArgument() {
        assertThatThrownBy(() -> resumeManager.resume("  "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
