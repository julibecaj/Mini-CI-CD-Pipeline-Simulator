package com.pipes.exception;

/**
 * Thrown when a pipeline execution encounters a fatal error.
 * Checked exception — callers must handle or declare it.
 * Satisfies R10: custom checked exception.
 */
public class PipelineExecutionException extends Exception {

    private final Long pipelineRunId;

    public PipelineExecutionException(Long pipelineRunId, String message) {
        super(message);
        this.pipelineRunId = pipelineRunId;
    }

    public PipelineExecutionException(Long pipelineRunId, String message, Throwable cause) {
        super(message, cause);
        this.pipelineRunId = pipelineRunId;
    }

    public Long getPipelineRunId() { return pipelineRunId; }
}
