package com.pipes.service;

import com.pipes.dto.RunDto;
import com.pipes.entity.Pipeline;
import com.pipes.entity.PipelineRun;
import com.pipes.entity.User;
import com.pipes.exception.PipesAccessDeniedException;
import com.pipes.exception.ResourceNotFoundException;
import com.pipes.repository.PipelineRepository;
import com.pipes.repository.PipelineRunRepository;
import com.pipes.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Read-side service for PipelineRun history and statistics.
 * Demonstrates R4 (Stream aggregations) and R6 (JDBC/JPA via repositories).
 */
@Service
@Transactional(readOnly = true)
public class RunService {

    private final PipelineRunRepository runRepository;
    private final PipelineRepository pipelineRepository;
    private final UserRepository userRepository;

    public RunService(PipelineRunRepository runRepository,
                      PipelineRepository pipelineRepository,
                      UserRepository userRepository) {
        this.runRepository = runRepository;
        this.pipelineRepository = pipelineRepository;
        this.userRepository = userRepository;
    }

    private User currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));
    }

    /** All runs for a specific pipeline (must be owned by the caller). */
    public List<RunDto.RunSummary> listForPipeline(Long pipelineId) {
        User user = currentUser();
        Pipeline pipeline = pipelineRepository.findByIdAndOwner(pipelineId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Pipeline", pipelineId));

        return runRepository.findByPipelineOrderByStartedAtDesc(pipeline)
                .stream()
                .map(RunDto::toSummary)   // R3 — method reference
                .collect(Collectors.toList());
    }

    /** Detailed view of a single run. */
    public RunDto.RunResponse getById(Long runId) {
        User user = currentUser();
        PipelineRun run = runRepository.findById(runId)
                .orElseThrow(() -> new ResourceNotFoundException("PipelineRun", runId));

        if (!run.getPipeline().getOwner().getId().equals(user.getId())) {
            throw new PipesAccessDeniedException("You do not own run " + runId);
        }

        return RunDto.toResponse(run);
    }

    /** Recent runs across ALL pipelines of the current user (dashboard feed). */
    public List<RunDto.RunSummary> recentRuns(int limit) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        return runRepository.findByPipeline_Owner_UsernameOrderByStartedAtDesc(username)
                .stream()
                .limit(limit)             // R4 — Stream.limit
                .map(RunDto::toSummary)
                .collect(Collectors.toList());
    }

    /** Aggregated statistics for the current user's dashboard. */
    public Map<String, Object> dashboardStats() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        List<PipelineRun> allRuns =
                runRepository.findByPipeline_Owner_UsernameOrderByStartedAtDesc(username);

        // R4: Stream terminal operations — groupingBy + counting
        Map<String, Long> byStatus = allRuns.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getStatus().name(),  // R3 — lambda key extractor
                        Collectors.counting()));

        long total   = allRuns.size();
        long success = byStatus.getOrDefault("SUCCESS", 0L);
        long failed  = byStatus.getOrDefault("FAILED",  0L);
        long running = byStatus.getOrDefault("RUNNING", 0L);

        double successRate = total == 0 ? 0.0 : (double) success / total * 100;

        return Map.of(
                "totalRuns",   total,
                "successRuns", success,
                "failedRuns",  failed,
                "runningRuns", running,
                "successRate", Math.round(successRate * 10) / 10.0
        );
    }

    @Transactional
    public void deleteRun(Long runId) {
        User user = currentUser();

        PipelineRun run = runRepository.findById(runId)
                .orElseThrow(() -> new ResourceNotFoundException("PipelineRun", runId));

        if (!run.getPipeline().getOwner().getId().equals(user.getId())) {
            throw new PipesAccessDeniedException("You do not own run " + runId);
        }

        runRepository.delete(run);
    }

    @Transactional
    public void cancelRun(Long runId) {
        User user = currentUser();

        PipelineRun run = runRepository.findById(runId)
                .orElseThrow(() -> new ResourceNotFoundException("PipelineRun", runId));

        if (!run.getPipeline().getOwner().getId().equals(user.getId())) {
            throw new PipesAccessDeniedException("You do not own run " + runId);
        }

        if (run.getStatus() == PipelineRun.RunStatus.SUCCESS ||
                run.getStatus() == PipelineRun.RunStatus.FAILED ||
                run.getStatus() == PipelineRun.RunStatus.CANCELLED) {
            return;
        }

        run.setStatus(PipelineRun.RunStatus.CANCELLED);
        run.setFinishedAt(java.time.Instant.now());
        run.appendLog("[PIPES] Run cancelled by user.");
        runRepository.save(run);
    }


}
