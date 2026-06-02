package com.pipes.dto;

import com.pipes.entity.JobResult;
import com.pipes.entity.PipelineRun;
import com.pipes.entity.StageResult;

import java.time.Instant;
import java.util.List;

/**
 * DTOs for PipelineRun read and trigger operations (R8).
 */
public class RunDto {

    public record RunResponse(
            Long     id,
            Long     pipelineId,
            String   pipelineName,
            String   status,
            Instant  startedAt,
            Instant  finishedAt,
            String   log,
            List<StageResultResponse> stageResults
    ) {}

    public record StageResultResponse(
            Long     id,
            String   stageName,
            int      stagePosition,
            String   status,
            Instant  startedAt,
            Instant  finishedAt,
            List<JobResultResponse> jobResults
    ) {}

    public record JobResultResponse(
            Long     id,
            String   jobName,
            String   command,
            String   status,
            Instant  startedAt,
            Instant  finishedAt,
            Integer  exitCode,
            String   output
    ) {}

    public record RunSummary(
            Long     id,
            Long     pipelineId,
            String   pipelineName,
            String   status,
            Instant  startedAt,
            Instant  finishedAt
    ) {}

    // ── Mapper helpers ────────────────────────────────────────────────────────

    public static RunResponse toResponse(PipelineRun run) {
        List<StageResultResponse> stages = run.getStageResults().stream()
                .map(RunDto::toStageResponse)
                .toList();

        return new RunResponse(
                run.getId(),
                run.getPipeline().getId(),
                run.getPipeline().getName(),
                run.getStatus().name(),
                run.getStartedAt(),
                run.getFinishedAt(),
                run.getLog(),
                stages);
    }

    public static StageResultResponse toStageResponse(StageResult sr) {
        List<JobResultResponse> jobs = sr.getJobResults().stream()
                .map(RunDto::toJobResponse)
                .toList();

        return new StageResultResponse(
                sr.getId(), sr.getStageName(), sr.getStagePosition(),
                sr.getStatus().name(), sr.getStartedAt(), sr.getFinishedAt(), jobs);
    }

    public static JobResultResponse toJobResponse(JobResult jr) {
        return new JobResultResponse(
                jr.getId(), jr.getJobName(), jr.getCommand(),
                jr.getStatus().name(), jr.getStartedAt(), jr.getFinishedAt(),
                jr.getExitCode(), jr.getOutput());
    }

    public static RunSummary toSummary(PipelineRun run) {
        return new RunSummary(
                run.getId(),
                run.getPipeline().getId(),
                run.getPipeline().getName(),
                run.getStatus().name(),
                run.getStartedAt(),
                run.getFinishedAt());
    }
}
