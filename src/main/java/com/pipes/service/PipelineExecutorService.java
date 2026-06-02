package com.pipes.service;

import com.pipes.entity.*;
import com.pipes.exception.ResourceNotFoundException;
import com.pipes.pattern.JobExecutionStrategy;
import com.pipes.pattern.PipelineRunBuilder;
import com.pipes.pattern.RunEventListener;
import com.pipes.repository.PipelineRepository;
import com.pipes.repository.PipelineRunRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Executes pipelines asynchronously.
 *
 * Demonstrates:
 *  R3  — Lambda expressions (CompletableFuture suppliers, Predicate)
 *  R4  — Stream API (collecting futures, checking results)
 *  R5  — ExecutorService + CompletableFuture for concurrent job execution
 *  R6  — JDBC/JPA writes via repositories on each status change
 *  R9  — Strategy (JobExecutionStrategy), Observer (RunEventListener),
 *          Builder (PipelineRunBuilder)
 *  R10 — PipelineExecutionException (checked), custom exception handling
 */
@Service
public class PipelineExecutorService {

    private static final Logger log = LoggerFactory.getLogger(PipelineExecutorService.class);

    private final ExecutorService executor;
    private final PipelineRepository pipelineRepository;
    private final PipelineRunRepository runRepository;
    private final JobExecutionStrategy jobStrategy;   // Strategy (R9)

    // Observer registry (R9) — thread-safe list
    private final List<RunEventListener> listeners = new CopyOnWriteArrayList<>();

    public PipelineExecutorService(
            @Qualifier("pipelineExecutor") ExecutorService executor,
            PipelineRepository pipelineRepository,
            PipelineRunRepository runRepository,
            JobExecutionStrategy jobStrategy) {
        this.executor = executor;
        this.pipelineRepository = pipelineRepository;
        this.runRepository = runRepository;
        this.jobStrategy = jobStrategy;
    }

    // ── Observer management ───────────────────────────────────────────────────

    public void addListener(RunEventListener listener)    { listeners.add(listener); }
    public void removeListener(RunEventListener listener) { listeners.remove(listener); }

    private void notifyRunChanged(PipelineRun run) {
        listeners.forEach(l -> l.onRunStatusChanged(run));   // R3 — lambda
    }
    private void notifyStageChanged(StageResult sr) {
        listeners.forEach(l -> l.onStageStatusChanged(sr));
    }
    private void notifyJobChanged(JobResult jr) {
        listeners.forEach(l -> l.onJobStatusChanged(jr));
    }

    // ── Trigger ───────────────────────────────────────────────────────────────

    /**
     * Create a PipelineRun and submit it to the thread pool asynchronously.
     * Returns immediately with the run id — the client polls for status.
     */
    @Transactional
    public PipelineRun trigger(Long pipelineId) {
        Pipeline pipeline = pipelineRepository.findById(pipelineId)
                .orElseThrow(() -> new ResourceNotFoundException("Pipeline", pipelineId));

        // Builder pattern (R9) constructs the full run object graph
        PipelineRun run = PipelineRunBuilder.forPipeline(pipeline)
                .triggeredBy("manual")
                .build();

        PipelineRun saved = runRepository.save(run);

        // Submit asynchronously so the HTTP response returns immediately (R5)
        executor.submit(() -> executeRun(saved.getId()));

        return saved;
    }

    // ── Execution ─────────────────────────────────────────────────────────────

    /**
     * Main execution loop — runs in a worker thread from the ExecutorService (R5).
     * Fetches a fresh instance from the DB so JPA operates within this thread's context.
     */
    private void executeRun(Long runId) {
        // Each execution runs in its own thread — fetch from DB with a new transaction
        PipelineRun run = runRepository.findById(runId).orElse(null);
        if (run == null) {
            log.error("PipelineRun {} not found in executor thread.", runId);
            return;
        }

        run.setStatus(PipelineRun.RunStatus.RUNNING);
        runRepository.save(run);
        notifyRunChanged(run);

        // R4: Stream to get stages sorted by position
        List<StageResult> sortedStages = run.getStageResults().stream()
                .sorted((a, b) -> Integer.compare(a.getStagePosition(), b.getStagePosition()))
                .collect(Collectors.toList());

        boolean pipelineFailed = false;

        for (StageResult stageResult : sortedStages) {
            if (pipelineFailed) {
                stageResult.setStatus(PipelineRun.RunStatus.CANCELLED);
                runRepository.save(run);
                notifyStageChanged(stageResult);
                continue;
            }

            pipelineFailed = !executeStage(run, stageResult);
        }

        run.setStatus(pipelineFailed ? PipelineRun.RunStatus.FAILED : PipelineRun.RunStatus.SUCCESS);
        run.setFinishedAt(Instant.now());
        runRepository.save(run);
        notifyRunChanged(run);

        log.info("Pipeline run {} finished with status {}", runId, run.getStatus());
    }

    /**
     * Execute all jobs in a stage concurrently (R5 — CompletableFuture).
     *
     * @return true if ALL jobs succeeded, false otherwise
     */
    private boolean executeStage(PipelineRun run, StageResult stageResult) {
        stageResult.setStatus(PipelineRun.RunStatus.RUNNING);
        stageResult.setStartedAt(Instant.now());
        runRepository.save(run);
        notifyStageChanged(stageResult);

        // Find the matching stage definition to get Job entities
        Stage stageDef = run.getPipeline().getStages().stream()
                .filter(s -> s.getName().equals(stageResult.getStageName())
                          && s.getPosition() == stageResult.getStagePosition())
                .findFirst()
                .orElse(null);

        if (stageDef == null || stageDef.getJobs().isEmpty()) {
            stageResult.setStatus(PipelineRun.RunStatus.SUCCESS);
            stageResult.setFinishedAt(Instant.now());
            runRepository.save(run);
            return true;
        }

        // R5: Submit each job as a CompletableFuture on the shared ExecutorService
        // R3: Lambda as Supplier<Boolean>
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();

        for (int i = 0; i < stageDef.getJobs().size(); i++) {
            Job job = stageDef.getJobs().get(i);
            JobResult jobResult = stageResult.getJobResults().size() > i
                    ? stageResult.getJobResults().get(i)
                    : new JobResult(stageResult, job.getName(), job.getCommand());

            CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(
                    () -> executeJob(run, job, jobResult),  // R3 — lambda supplier
                    executor                                 // R5 — our thread pool
            );
            futures.add(future);
        }

        // R4: Stream to collect results; allOf waits for all jobs (R5)
        CompletableFuture<Void> allJobs = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0]));

        try {
            allJobs.get(); // block this stage-thread until all job-threads finish
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            stageResult.setStatus(PipelineRun.RunStatus.FAILED);
            stageResult.setFinishedAt(Instant.now());
            runRepository.save(run);
            return false;
        } catch (ExecutionException e) {
            log.error("Stage '{}' execution error: {}", stageResult.getStageName(), e.getMessage(), e);
        }

        // R4: check if any job failed
        boolean allSucceeded = futures.stream()
                .map(f -> {
                    try { return f.get(); }
                    catch (Exception ex) { return false; }
                })
                .allMatch(Boolean::booleanValue);

        stageResult.setStatus(allSucceeded
                ? PipelineRun.RunStatus.SUCCESS
                : PipelineRun.RunStatus.FAILED);
        stageResult.setFinishedAt(Instant.now());
        runRepository.save(run);
        notifyStageChanged(stageResult);

        return allSucceeded;
    }

    /**
     * Execute a single Job using the injected Strategy (R9 — Strategy pattern).
     *
     * @return true if the job succeeded
     */
    private boolean executeJob(PipelineRun run, Job job, JobResult jobResult) {
        jobResult.setStatus(PipelineRun.RunStatus.RUNNING);
        jobResult.setStartedAt(Instant.now());
        runRepository.save(run);
        notifyJobChanged(jobResult);

        try {
            jobStrategy.execute(job, jobResult);  // Strategy (R9) — delegate to LocalShellStrategy
        } catch (Exception ex) {
            // R10: exception is caught, context added, not swallowed
            log.error("Job '{}' threw an unexpected exception: {}", job.getName(), ex.getMessage(), ex);
            jobResult.setStatus(PipelineRun.RunStatus.FAILED);
            jobResult.setOutput(jobResult.getOutput() + "\n[PIPES] Internal error: " + ex.getMessage());
            jobResult.setFinishedAt(Instant.now());
        }

        // Append a summary line to the run-level log (thread-safe via synchronized, R5)
        String icon = jobResult.getStatus() == PipelineRun.RunStatus.SUCCESS ? "✓" : "✗";
        run.appendLog(String.format("[%s] %s/%s — exit %s",
                icon, jobResult.getStageResult().getStageName(),
                jobResult.getJobName(), jobResult.getExitCode()));

        runRepository.save(run);
        notifyJobChanged(jobResult);

        return jobResult.getStatus() == PipelineRun.RunStatus.SUCCESS;
    }
}
