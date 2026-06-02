package com.pipes.pattern;

import com.pipes.entity.Job;
import com.pipes.entity.JobResult;
import com.pipes.entity.PipelineRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Concrete Strategy (R9) that executes a job as a real OS shell command.
 * Uses ProcessBuilder to spawn a child process and capture its output.
 *
 * On Windows the shell is cmd /c; on Unix it is sh -c.
 */
@Component
public class LocalShellStrategy implements JobExecutionStrategy {

    private static final Logger log = LoggerFactory.getLogger(LocalShellStrategy.class);

    @Override
    public void execute(Job job, JobResult result) throws Exception {
        result.setStartedAt(Instant.now());
        result.setStatus(PipelineRun.RunStatus.RUNNING);

        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        String[] cmd = isWindows
                ? new String[]{"cmd", "/c", job.getCommand()}
                : new String[]{"sh", "-c", job.getCommand()};

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true); // merge stderr into stdout

        StringBuilder output = new StringBuilder();

        try {
            Process process = pb.start();

            // Read output line-by-line as the process runs
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    log.debug("[job={}] {}", job.getName(), line);
                }
            }

            boolean finished = process.waitFor(job.getTimeoutSeconds(), TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                output.append("\n[PIPES] Job timed out after ")
                      .append(job.getTimeoutSeconds()).append("s — killed.\n");
                result.setExitCode(-1);
                result.setStatus(PipelineRun.RunStatus.FAILED);
            } else {
                int exitCode = process.exitValue();
                result.setExitCode(exitCode);
                result.setStatus(exitCode == 0
                        ? PipelineRun.RunStatus.SUCCESS
                        : PipelineRun.RunStatus.FAILED);
            }

        } catch (Exception ex) {
            output.append("\n[PIPES] Failed to start process: ").append(ex.getMessage()).append("\n");
            result.setExitCode(-2);
            result.setStatus(PipelineRun.RunStatus.FAILED);
            log.error("Process start failed for job '{}': {}", job.getName(), ex.getMessage(), ex);
            throw ex; // re-throw so the executor knows the infrastructure failed
        } finally {
            result.setOutput(output.toString());
            result.setFinishedAt(Instant.now());
        }
    }
}
