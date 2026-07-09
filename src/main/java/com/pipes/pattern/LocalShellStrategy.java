package com.pipes.pattern;

import com.pipes.entity.Job;
import com.pipes.entity.JobResult;
import com.pipes.entity.PipelineRun;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class LocalShellStrategy implements JobExecutionStrategy {

    @Override
    public void execute(Job job, JobResult result) throws Exception {
        result.setStartedAt(Instant.now());
        result.setStatus(PipelineRun.RunStatus.RUNNING);

        StringBuilder output = new StringBuilder();

        output.append("[PIPES] Starting job: ")
                .append(job.getName())
                .append("\n");

        output.append("[PIPES] Simulated command: ")
                .append(job.getCommand())
                .append("\n");

        Thread.sleep(ThreadLocalRandom.current().nextLong(800, 1800));

        boolean shouldFail =
                job.getCommand() != null &&
                        job.getCommand().toLowerCase().contains("fail");

        if (shouldFail) {
            result.setExitCode(1);
            result.setStatus(PipelineRun.RunStatus.FAILED);
            output.append("[PIPES] Simulated job failure.\n");
        } else {
            result.setExitCode(0);
            result.setStatus(PipelineRun.RunStatus.SUCCESS);
            output.append("[PIPES] Job completed successfully.\n");
        }

        result.setOutput(output.toString());
        result.setFinishedAt(Instant.now());
    }
}