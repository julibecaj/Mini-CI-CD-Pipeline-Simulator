package com.pipes.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stage_results")
public class StageResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pipeline_run_id", nullable = false)
    private PipelineRun pipelineRun;

    @Column(nullable = false, length = 120)
    private String stageName;

    @Column(nullable = false)
    private int stagePosition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PipelineRun.RunStatus status = PipelineRun.RunStatus.PENDING;

    private Instant startedAt;
    private Instant finishedAt;

    @OneToMany(mappedBy = "stageResult", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("id ASC")
    private List<JobResult> jobResults = new ArrayList<>();

    public StageResult() {}

    public StageResult(PipelineRun run, String stageName, int stagePosition) {
        this.pipelineRun = run;
        this.stageName = stageName;
        this.stagePosition = stagePosition;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public PipelineRun getPipelineRun() { return pipelineRun; }
    public void setPipelineRun(PipelineRun r) { this.pipelineRun = r; }
    public String getStageName() { return stageName; }
    public void setStageName(String stageName) { this.stageName = stageName; }
    public int getStagePosition() { return stagePosition; }
    public void setStagePosition(int p) { this.stagePosition = p; }
    public PipelineRun.RunStatus getStatus() { return status; }
    public void setStatus(PipelineRun.RunStatus status) { this.status = status; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getFinishedAt() { return finishedAt; }
    public void setFinishedAt(Instant finishedAt) { this.finishedAt = finishedAt; }
    public List<JobResult> getJobResults() { return jobResults; }
    public void setJobResults(List<JobResult> jobResults) { this.jobResults = jobResults; }
}