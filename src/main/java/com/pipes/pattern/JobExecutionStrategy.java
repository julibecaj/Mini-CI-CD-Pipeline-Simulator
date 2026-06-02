package com.pipes.pattern;

import com.pipes.entity.Job;
import com.pipes.entity.JobResult;

/**
 * Strategy interface (GoF — R9) for executing a single Job.
 *
 * Why Strategy? Different environments might execute jobs differently:
 *   - LocalShellStrategy  → spawns a real OS process (default)
 *   - SimulatedStrategy   → fakes execution for demo / testing
 *
 * Swapping strategies at runtime requires zero changes to the executor.
 */
public interface JobExecutionStrategy {

    /**
     * Execute the given job and populate the result object.
     *
     * @param job    the job definition (command, timeout)
     * @param result the result object to be mutated with output and exit code
     * @throws Exception if the execution infrastructure itself fails
     */
    void execute(Job job, JobResult result) throws Exception;
}
