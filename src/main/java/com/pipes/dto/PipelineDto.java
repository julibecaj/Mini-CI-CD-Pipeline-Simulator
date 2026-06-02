package com.pipes.dto;

import com.pipes.entity.Pipeline;
import com.pipes.entity.Stage;
import com.pipes.entity.Job;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

/**
 * DTOs for Pipeline CRUD operations (R8).
 */
public class PipelineDto {

    // ── Request ───────────────────────────────────────────────────────────────

    public record CreateRequest(
            @NotBlank @Size(max = 120)  String name,
                                        String description,
                                        String targetBranch,
            @NotNull @Valid             List<StageRequest> stages
    ) {}

    public record StageRequest(
            @NotBlank @Size(max = 120)  String name,
            @NotNull @Valid             List<JobRequest> jobs
    ) {}

    public record JobRequest(
            @NotBlank @Size(max = 120)  String name,
            @NotBlank @Size(max = 500)  String command,
                                        Integer timeoutSeconds   // nullable → defaults to 60
    ) {}

    // ── Response ──────────────────────────────────────────────────────────────

    public record PipelineResponse(
            Long    id,
            String  name,
            String  description,
            String  targetBranch,
            String  ownerUsername,
            Instant createdAt,
            Instant updatedAt,
            List<StageResponse> stages
    ) {}

    public record StageResponse(
            Long   id,
            String name,
            int    position,
            List<JobResponse> jobs
    ) {}

    public record JobResponse(
            Long   id,
            String name,
            String command,
            int    timeoutSeconds
    ) {}

    public record PipelineSummary(
            Long    id,
            String  name,
            String  description,
            String  targetBranch,
            Instant updatedAt,
            long    totalRuns,
            long    successRuns
    ) {}

    // ── Mapper helpers ────────────────────────────────────────────────────────

    public static PipelineResponse toResponse(Pipeline p) {
        // Stream API (R4): map child collections to response records
        List<StageResponse> stages = p.getStages().stream()
                .map(PipelineDto::toStageResponse)
                .toList();

        return new PipelineResponse(
                p.getId(), p.getName(), p.getDescription(),
                p.getTargetBranch(), p.getOwner().getUsername(),
                p.getCreatedAt(), p.getUpdatedAt(), stages);
    }

    public static StageResponse toStageResponse(Stage s) {
        List<JobResponse> jobs = s.getJobs().stream()
                .map(PipelineDto::toJobResponse)
                .toList();
        return new StageResponse(s.getId(), s.getName(), s.getPosition(), jobs);
    }

    public static JobResponse toJobResponse(Job j) {
        return new JobResponse(j.getId(), j.getName(), j.getCommand(), j.getTimeoutSeconds());
    }
}
