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
        pb.redirectErrorStream(true);

        StringBuilder output = new StringBuilder();

        try {
            Process process = pb.start();

            Thread outputThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                } catch (Exception ignored) {}
            });

            outputThread.start();

            boolean finished = process.waitFor(job.getTimeoutSeconds(), TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                result.setExitCode(-1);
                result.setStatus(PipelineRun.RunStatus.FAILED);
                output.append("\n[PIPES] Job timed out after ")
                        .append(job.getTimeoutSeconds())
                        .append("s — killed.\n");
            } else {
                outputThread.join(1000);
                int exitCode = process.exitValue();
                result.setExitCode(exitCode);
                result.setStatus(exitCode == 0
                        ? PipelineRun.RunStatus.SUCCESS
                        : PipelineRun.RunStatus.FAILED);
            }

        } catch (Exception ex) {
            result.setExitCode(-2);
            result.setStatus(PipelineRun.RunStatus.FAILED);
            output.append("\n[PIPES] Failed: ").append(ex.getMessage()).append("\n");
            throw ex;
        } finally {
            result.setOutput(output.toString());
            result.setFinishedAt(Instant.now());
        }
    }
}
