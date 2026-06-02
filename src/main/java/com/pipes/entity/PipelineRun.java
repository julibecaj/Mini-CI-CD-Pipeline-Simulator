package com.pipes.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pipeline_runs")
public class PipelineRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pipeline_id", nullable = false)
    private Pipeline pipeline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RunStatus status = RunStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private Instant startedAt = Instant.now();

    private Instant finishedAt;

    @Column(columnDefinition = "TEXT")
    private String log = "";

    @OneToMany(mappedBy = "pipelineRun", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("id ASC")
    private List<StageResult> stageResults = new ArrayList<>();

    public PipelineRun() {}
    public PipelineRun(Pipeline pipeline) { this.pipeline = pipeline; }

    public synchronized void appendLog(String line) {
        this.log = this.log + line + "\n";
    }

    public enum RunStatus { PENDING, RUNNING, SUCCESS, FAILED, CANCELLED }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Pipeline getPipeline() { return pipeline; }
    public void setPipeline(Pipeline pipeline) { this.pipeline = pipeline; }
    public RunStatus getStatus() { return status; }
    public void setStatus(RunStatus status) { this.status = status; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getFinishedAt() { return finishedAt; }
    public void setFinishedAt(Instant finishedAt) { this.finishedAt = finishedAt; }
    public String getLog() { return log; }
    public void setLog(String log) { this.log = log; }
    public List<StageResult> getStageResults() { return stageResults; }
    public void setStageResults(List<StageResult> sr) { this.stageResults = sr; }
}