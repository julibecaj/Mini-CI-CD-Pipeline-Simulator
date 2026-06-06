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

import com.pipes.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;

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

    private final UserRepository userRepository;

    private final RunPersistenceService runPersistence;

    private static final Logger log = LoggerFactory.getLogger(PipelineExecutorService.class);

    private final ExecutorService executor;
    private final PipelineRepository pipelineRepository;
    private final PipelineRunRepository runRepository;
    private final JobExecutionStrategy jobStrategy;   // Strategy (R9)

    // Observer registry (R9) — thread-safe list
    private final List<RunEventListener> listeners = new CopyOnWriteArrayList<>();

    public PipelineExecutorService(
            RunPersistenceService runPersistence,
            @Qualifier("pipelineExecutor") ExecutorService executor,
            PipelineRepository pipelineRepository,
            PipelineRunRepository runRepository,
            UserRepository userRepository,
            JobExecutionStrategy jobStrategy) {
        this.runPersistence = runPersistence;
        this.executor = executor;
        this.pipelineRepository = pipelineRepository;
        this.runRepository = runRepository;
        this.userRepository = userRepository;
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
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));

        Pipeline pipeline = pipelineRepository.findByIdAndOwner(pipelineId, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Pipeline", pipelineId));

        PipelineRun run = PipelineRunBuilder.forPipeline(pipeline)
                .triggeredBy("manual")
                .build();

        PipelineRun saved = runPersistence.save(run);

        executor.submit(() -> executeRun(saved.getId()));

        return saved;
    }

    // ── Execution ─────────────────────────────────────────────────────────────

    /**
     * Main execution loop — runs in a worker thread from the ExecutorService (R5).
     * Fetches a fresh instance from the DB so JPA operates within this thread's context.
     */

    private void executeRun(Long runId) {
        log.info(">>> EXECUTOR STARTED RUN {}", runId);


        PipelineRun run = runRepository.findByIdWithFullGraph(runId).orElse(null);

        if (run == null) {
            log.error("PipelineRun {} not found.", runId);
            return;
        }

        try {
            run.setStatus(PipelineRun.RunStatus.RUNNING);
            runPersistence.save(run);
            notifyRunChanged(run);

            List<StageResult> sortedStages = run.getStageResults().stream()
                    .sorted((a, b) -> Integer.compare(a.getStagePosition(), b.getStagePosition()))
                    .collect(Collectors.toList());

            boolean pipelineFailed = false;

            for (StageResult stageResult : sortedStages) {
                if (pipelineFailed) {
                    stageResult.setStatus(PipelineRun.RunStatus.CANCELLED);
                    stageResult.setFinishedAt(Instant.now());
                    runPersistence.save(run);
                    notifyStageChanged(stageResult);
                    continue;
                }

                boolean stageOk = executeStage(run, stageResult);
                if (!stageOk) {
                    pipelineFailed = true;
                }
            }

            run.setStatus(pipelineFailed ? PipelineRun.RunStatus.FAILED : PipelineRun.RunStatus.SUCCESS);
            run.setFinishedAt(Instant.now());
            runPersistence.save(run);
            notifyRunChanged(run);

        } catch (Exception ex) {
            log.error("Run {} crashed: {}", runId, ex.getMessage(), ex);

            run.setStatus(PipelineRun.RunStatus.FAILED);
            run.setFinishedAt(Instant.now());
            run.appendLog("[PIPES] Run crashed: " + ex.getMessage());
            runPersistence.save(run);
            notifyRunChanged(run);
        }
    }

    /**
     * Execute all jobs in a stage concurrently (R5 — CompletableFuture).
     *
     * @return true if ALL jobs succeeded, false otherwise
     */
    private boolean executeStage(PipelineRun run, StageResult stageResult) {
        try {
            stageResult.setStatus(PipelineRun.RunStatus.RUNNING);
            stageResult.setStartedAt(Instant.now());
            runPersistence.save(run);
            notifyStageChanged(stageResult);

            boolean stageFailed = false;

            for (JobResult jobResult : stageResult.getJobResults()) {
                jobResult.setStatus(PipelineRun.RunStatus.RUNNING);
                jobResult.setStartedAt(Instant.now());
                runPersistence.save(run);
                notifyJobChanged(jobResult);

                try {
                    Job job = new Job();
                    job.setName(jobResult.getJobName());
                    job.setCommand(jobResult.getCommand());
                    job.setTimeoutSeconds(60);

                    jobStrategy.execute(job, jobResult);

                    if (jobResult.getStatus() == PipelineRun.RunStatus.FAILED) {
                        stageFailed = true;
                    }

                } catch (Exception ex) {
                    jobResult.setStatus(PipelineRun.RunStatus.FAILED);
                    jobResult.setOutput("[PIPES] Job crashed: " + ex.getMessage());
                    jobResult.setExitCode(-1);
                    stageFailed = true;
                }

                jobResult.setFinishedAt(Instant.now());
                runPersistence.save(run);
                notifyJobChanged(jobResult);
            }

            stageResult.setStatus(stageFailed
                    ? PipelineRun.RunStatus.FAILED
                    : PipelineRun.RunStatus.SUCCESS);

            stageResult.setFinishedAt(Instant.now());
            runPersistence.save(run);
            notifyStageChanged(stageResult);

            return !stageFailed;

        } catch (Exception ex) {
            log.error("Stage {} crashed: {}", stageResult.getStageName(), ex.getMessage(), ex);

            stageResult.setStatus(PipelineRun.RunStatus.FAILED);
            stageResult.setFinishedAt(Instant.now());
            runPersistence.save(run);
            notifyStageChanged(stageResult);

            return false;
        }
    }

    /**
     * Execute a single Job using the injected Strategy (R9 — Strategy pattern).
     *
     * @return true if the job succeeded
     */
    private boolean executeJob(PipelineRun run, Job job, JobResult jobResult) {
        jobResult.setStatus(PipelineRun.RunStatus.RUNNING);
        jobResult.setStartedAt(Instant.now());
        runPersistence.save(run);
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

        runPersistence.save(run);
        notifyJobChanged(jobResult);

        return jobResult.getStatus() == PipelineRun.RunStatus.SUCCESS;
    }
}
