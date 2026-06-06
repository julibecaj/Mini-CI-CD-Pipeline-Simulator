package com.pipes.controller;

import com.pipes.dto.RunDto;
import com.pipes.entity.PipelineRun;
import com.pipes.service.PipelineExecutorService;
import com.pipes.service.RunService;
import com.pipes.util.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST endpoints for pipeline run operations (R8).
 *
 * POST /api/pipelines/{id}/runs        — trigger a new run
 * GET  /api/pipelines/{id}/runs        — list runs for a pipeline
 * GET  /api/runs/{runId}               — get full run detail (with job logs)
 * GET  /api/runs/recent                — last N runs across all pipelines
 * GET  /api/runs/stats                 — dashboard stats
 */
@RestController
public class RunController {

    private final PipelineExecutorService executorService;
    private final RunService runService;

    public RunController(PipelineExecutorService executorService, RunService runService) {
        this.executorService = executorService;
        this.runService = runService;
    }

    /** Trigger a new run for the given pipeline. Returns immediately with the new run id. */
    @PostMapping("/api/pipelines/{id}/runs")
    public ResponseEntity<ApiResponse<RunDto.RunSummary>> trigger(@PathVariable Long id) {
        PipelineRun run = executorService.trigger(id);
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(ApiResponse.ok("Pipeline triggered.", RunDto.toSummary(run)));
    }

    /** List all runs for a pipeline (summary only — no logs). */
    @GetMapping("/api/pipelines/{id}/runs")
    public ResponseEntity<ApiResponse<List<RunDto.RunSummary>>> listForPipeline(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(runService.listForPipeline(id)));
    }

    /** Full detail for one run, including all job outputs. Used for the run detail page / polling. */
    @GetMapping("/api/runs/{runId}")
    public ResponseEntity<ApiResponse<RunDto.RunResponse>> getById(@PathVariable Long runId) {
        return ResponseEntity.ok(ApiResponse.ok(runService.getById(runId)));
    }

    /** Recent runs across all the current user's pipelines (dashboard feed). */
    @GetMapping("/api/runs/recent")
    public ResponseEntity<ApiResponse<List<RunDto.RunSummary>>> recent(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(runService.recentRuns(limit)));
    }

    /** Aggregate statistics for the current user. */
    @GetMapping("/api/runs/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> stats() {
        return ResponseEntity.ok(ApiResponse.ok(runService.dashboardStats()));
    }


    @PostMapping("/api/runs/{runId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancel(@PathVariable Long runId) {
        runService.cancelRun(runId);
        return ResponseEntity.ok(ApiResponse.ok("Run cancelled.", null));
    }

    @DeleteMapping("/api/runs/{runId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long runId) {
        runService.deleteRun(runId);
        return ResponseEntity.ok(ApiResponse.ok("Run deleted.", null));
    }


}
