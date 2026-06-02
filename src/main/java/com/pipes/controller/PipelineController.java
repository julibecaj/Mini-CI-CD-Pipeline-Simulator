package com.pipes.controller;

import com.pipes.dto.PipelineDto;
import com.pipes.service.PipelineService;
import com.pipes.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST endpoints for Pipeline management (R8).
 *
 * GET    /api/pipelines          — list all pipelines for the current user
 * POST   /api/pipelines          — create a new pipeline
 * GET    /api/pipelines/{id}     — get a single pipeline (full detail)
 * PUT    /api/pipelinesD/{id}     — replace a pipeline definition
 * DELETE /api/pipelines/{id}     — delete a pipeline and all its runs
 * GET    /api/pipelines/search   — search by name keyword
 * GET    /api/pipelines/stats    — aggregate stats for dashboard
 */
@RestController
@RequestMapping("/api/pipelines")
public class PipelineController {

    private final PipelineService pipelineService;

    public PipelineController(PipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PipelineDto.PipelineSummary>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(pipelineService.listForCurrentUser()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PipelineDto.PipelineResponse>> create(
            @Valid @RequestBody PipelineDto.CreateRequest req) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Pipeline created.", pipelineService.create(req)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PipelineDto.PipelineResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(pipelineService.getById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PipelineDto.PipelineResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody PipelineDto.CreateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Pipeline updated.", pipelineService.update(id, req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        pipelineService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Pipeline deleted.", null));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<PipelineDto.PipelineSummary>>> search(
            @RequestParam String q) {
        return ResponseEntity.ok(ApiResponse.ok(pipelineService.search(q)));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> stats() {
        return ResponseEntity.ok(ApiResponse.ok(pipelineService.stats()));
    }
}
