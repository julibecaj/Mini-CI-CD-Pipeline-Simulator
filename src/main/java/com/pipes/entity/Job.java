package com.pipes.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "jobs")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 120)
    private String name;

    @NotBlank
    @Column(nullable = false, length = 500)
    private String command;

    @Column(nullable = false)
    private int timeoutSeconds = 60;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stage_id", nullable = false)
    private Stage stage;

    public Job() {}

    public Job(String name, String command, int timeoutSeconds, Stage stage) {
        this.name = name;
        this.command = command;
        this.timeoutSeconds = timeoutSeconds;
        this.stage = stage;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }
    public int getTimeoutSeconds() { return timeoutSeconds; }
    public void setTimeoutSeconds(int t) { this.timeoutSeconds = t; }
    public Stage getStage() { return stage; }
    public void setStage(Stage stage) { this.stage = stage; }
}