package com.pipes.pattern;

import com.pipes.entity.Job;
import com.pipes.entity.Pipeline;
import com.pipes.entity.PipelineRun;
import com.pipes.entity.Stage;
import com.pipes.entity.StageResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder pattern (GoF — R9) for constructing a PipelineRun with its
 * nested StageResult / JobResult shells before any execution begins.
 *
 * Why Builder? A PipelineRun has a complex object graph (run → stages → jobs).
 * Using a builder separates the construction logic from the domain objects and
 * makes it easy to add optional configuration (e.g. triggered-by metadata)
 * without polluting the entity constructors.
 */
public class PipelineRunBuilder {

    private final Pipeline pipeline;
    private String triggeredBy = "manual";

    private PipelineRunBuilder(Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    /** Factory method — starts the builder for the given pipeline. */
    public static PipelineRunBuilder forPipeline(Pipeline pipeline) {
        return new PipelineRunBuilder(pipeline);
    }

    public PipelineRunBuilder triggeredBy(String triggeredBy) {
        this.triggeredBy = triggeredBy;
        return this;
    }

    /**
     * Construct and return a PipelineRun with empty StageResults / JobResults
     * initialised in PENDING status, ready for the executor to populate.
     */
    public PipelineRun build() {
        PipelineRun run = new PipelineRun(pipeline);
        run.setStatus(PipelineRun.RunStatus.PENDING);

        // Sort stages by position using a lambda comparator (R3)
        List<Stage> sorted = new ArrayList<>(pipeline.getStages());
        sorted.sort((a, b) -> Integer.compare(a.getPosition(), b.getPosition()));

        for (Stage stage : sorted) {
            StageResult sr = new StageResult(run, stage.getName(), stage.getPosition());
            sr.setStatus(PipelineRun.RunStatus.PENDING);

            for (Job job : stage.getJobs()) {
                com.pipes.entity.JobResult jr =
                        new com.pipes.entity.JobResult(sr, job.getName(), job.getCommand());
                jr.setStatus(PipelineRun.RunStatus.PENDING);
                sr.getJobResults().add(jr);
            }

            run.getStageResults().add(sr);
        }

        return run;
    }
}
