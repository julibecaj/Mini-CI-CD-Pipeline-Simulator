package com.pipes.pattern;

import com.pipes.entity.JobResult;
import com.pipes.entity.PipelineRun;
import com.pipes.entity.StageResult;

/**
 * Observer interface (GoF — R9).
 * Any component interested in pipeline execution events implements this.
 * The PipelineExecutorService (subject) notifies all registered observers
 * as stages and jobs transition through their lifecycle.
 */
public interface RunEventListener {

    /** Called when a PipelineRun changes status. */
    void onRunStatusChanged(PipelineRun run);

    /** Called when a StageResult changes status. */
    void onStageStatusChanged(StageResult stage);

    /** Called when a JobResult changes status or produces output. */
    void onJobStatusChanged(JobResult job);
}
