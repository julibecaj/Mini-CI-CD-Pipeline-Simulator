package com.pipes.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "job_results")
public class JobResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stage_result_id", nullable = false)
    private StageResult stageResult;

    @Column(nullable = false, length = 120)
    private String jobName;

    @Column(nullable = false, length = 500)
    private String command;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PipelineRun.RunStatus status = PipelineRun.RunStatus.PENDING;

    private Instant startedAt;
    private Instant finishedAt;
    private Integer exitCode;

    @Column(columnDefinition = "TEXT")
    private String output = "";

    public JobResult() {}

    public JobResult(StageResult stageResult, String jobName, String command) {
        this.stageResult = stageResult;
        this.jobName = jobName;
        this.command = command;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public StageResult getStageResult() { return stageResult; }
    public void setStageResult(StageResult sr) { this.stageResult = sr; }
    public String getJobName() { return jobName; }
    public void setJobName(String jobName) { this.jobName = jobName; }
    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }
    public PipelineRun.RunStatus getStatus() { return status; }
    public void setStatus(PipelineRun.RunStatus status) { this.status = status; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getFinishedAt() { return finishedAt; }
    public void setFinishedAt(Instant finishedAt) { this.finishedAt = finishedAt; }
    public Integer getExitCode() { return exitCode; }
    public void setExitCode(Integer exitCode) { this.exitCode = exitCode; }
    public String getOutput() { return output; }
    public void setOutput(String output) { this.output = output; }
}