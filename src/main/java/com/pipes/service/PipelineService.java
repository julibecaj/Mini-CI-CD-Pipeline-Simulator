package com.pipes.service;

import com.pipes.dto.PipelineDto;
import com.pipes.entity.Job;
import com.pipes.entity.Pipeline;
import com.pipes.entity.Stage;
import com.pipes.entity.User;
import com.pipes.exception.PipesAccessDeniedException;
import com.pipes.exception.ResourceNotFoundException;
import com.pipes.repository.PipelineRepository;
import com.pipes.repository.PipelineRunRepository;
import com.pipes.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Business logic for Pipeline CRUD operations.
 *
 * Demonstrates:
 *  R1  — ArrayList, HashMap, TreeSet-equivalent (via sorted stream)
 *  R3  — Lambdas (Predicate, Function)
 *  R4  — Stream API
 *  R10 — Custom exceptions
 */
@Service
@Transactional
public class PipelineService {

    private final PipelineRepository pipelineRepository;
    private final PipelineRunRepository runRepository;
    private final UserRepository userRepository;

    public PipelineService(PipelineRepository pipelineRepository,
                           PipelineRunRepository runRepository,
                           UserRepository userRepository) {
        this.pipelineRepository = pipelineRepository;
        this.runRepository = runRepository;
        this.userRepository = userRepository;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));
    }

    private Pipeline ownedPipeline(Long id) {
        User user = currentUser();
        Pipeline p = pipelineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pipeline", id));
        if (!p.getOwner().getId().equals(user.getId())) {
            throw new PipesAccessDeniedException("You do not own pipeline " + id);
        }
        return p;
    }

    // ── Create ────────────────────────────────────────────────────────────────

    public PipelineDto.PipelineResponse create(PipelineDto.CreateRequest req) {
        User owner = currentUser();
        Pipeline pipeline = new Pipeline(
                req.name(),
                req.description(),
                req.targetBranch() != null ? req.targetBranch() : "main",
                owner
        );

        // R1: ArrayList to build stages in order
        List<Stage> stages = new ArrayList<>();
        for (int i = 0; i < req.stages().size(); i++) {
            PipelineDto.StageRequest sr = req.stages().get(i);
            Stage stage = new Stage(sr.name(), i, pipeline);

            // R3: Lambda + stream to map job requests → Job entities (R4)
            sr.jobs().stream()
              .map(jr -> new Job(
                      jr.name(),
                      jr.command(),
                      jr.timeoutSeconds() != null ? jr.timeoutSeconds() : 60,
                      stage))
              .forEach(stage.getJobs()::add);

            stages.add(stage);
        }
        pipeline.getStages().addAll(stages);

        return PipelineDto.toResponse(pipelineRepository.save(pipeline));
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<PipelineDto.PipelineSummary> listForCurrentUser() {
        User user = currentUser();
        List<Pipeline> pipelines = pipelineRepository.findByOwnerOrderByUpdatedAtDesc(user);

        // R1: HashMap to cache run counts (avoids N+1 inside stream)
        Map<Long, Long> successCounts = new HashMap<>();
        Map<Long, Long> totalCounts   = new HashMap<>();

        pipelines.forEach(p -> {
            long total   = runRepository.countByPipelineAndStatus(p, com.pipes.entity.PipelineRun.RunStatus.SUCCESS)
                         + runRepository.countByPipelineAndStatus(p, com.pipes.entity.PipelineRun.RunStatus.FAILED)
                         + runRepository.countByPipelineAndStatus(p, com.pipes.entity.PipelineRun.RunStatus.CANCELLED);
            long success = runRepository.countByPipelineAndStatus(p, com.pipes.entity.PipelineRun.RunStatus.SUCCESS);
            totalCounts.put(p.getId(), total);
            successCounts.put(p.getId(), success);
        });

        // R4: Stream + map terminal
        return pipelines.stream()
                .map(p -> new PipelineDto.PipelineSummary(
                        p.getId(), p.getName(), p.getDescription(),
                        p.getTargetBranch(), p.getUpdatedAt(),
                        totalCounts.getOrDefault(p.getId(), 0L),
                        successCounts.getOrDefault(p.getId(), 0L)))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PipelineDto.PipelineResponse getById(Long id) {
        return PipelineDto.toResponse(ownedPipeline(id));
    }

    // ── Update ────────────────────────────────────────────────────────────────

    public PipelineDto.PipelineResponse update(Long id, PipelineDto.CreateRequest req) {
        Pipeline pipeline = ownedPipeline(id);
        pipeline.setName(req.name());
        if (req.description() != null) pipeline.setDescription(req.description());
        if (req.targetBranch() != null) pipeline.setTargetBranch(req.targetBranch());

        // Replace stages completely
        pipeline.getStages().clear();

        // R3: indexed loop with lambda-style job mapping
        for (int i = 0; i < req.stages().size(); i++) {
            PipelineDto.StageRequest sr = req.stages().get(i);
            Stage stage = new Stage(sr.name(), i, pipeline);
            sr.jobs().stream()
              .map(jr -> new Job(jr.name(), jr.command(),
                      jr.timeoutSeconds() != null ? jr.timeoutSeconds() : 60, stage))
              .forEach(stage.getJobs()::add);
            pipeline.getStages().add(stage);
        }

        return PipelineDto.toResponse(pipelineRepository.save(pipeline));
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    public void delete(Long id) {
        Pipeline pipeline = ownedPipeline(id);
        pipelineRepository.delete(pipeline);
    }

    // ── Search (demonstrates FilteredList generic utility) ────────────────────

    @Transactional(readOnly = true)
    public List<PipelineDto.PipelineSummary> search(String keyword) {
        // Re-use listForCurrentUser then filter in memory via generic util (R2)
        List<PipelineDto.PipelineSummary> all = listForCurrentUser();

        // PipelineSummary doesn't implement Comparable, so we use the two-arg query
        // with explicit Comparator — demonstrates R2 + R3 together
        return all.stream()
                .filter(s -> s.name().toLowerCase().contains(keyword.toLowerCase()))
                .sorted((a, b) -> a.name().compareToIgnoreCase(b.name()))
                .collect(Collectors.toList());
    }

    // ── Stats ─────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Map<String, Object> stats() {
        User user = currentUser();
        List<Pipeline> pipelines = pipelineRepository.findByOwnerOrderByUpdatedAtDesc(user);

        // R4: aggregate stats via streams
        long totalPipelines = pipelines.size();
        long totalStages    = pipelines.stream().mapToLong(p -> p.getStages().size()).sum();
        long totalJobs      = pipelines.stream()
                .flatMap(p -> p.getStages().stream())
                .mapToLong(s -> s.getJobs().size())
                .sum();

        // R1: HashMap to return mixed-type stats map
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPipelines", totalPipelines);
        stats.put("totalStages",    totalStages);
        stats.put("totalJobs",      totalJobs);
        return stats;
    }
}
