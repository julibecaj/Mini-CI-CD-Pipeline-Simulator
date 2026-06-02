package com.pipes.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Configures the shared thread pool used by PipelineExecutorService (R5).
 *
 * Uses ThreadPoolExecutor directly (rather than Executors.newFixedThreadPool)
 * so we can control the queue capacity and rejection policy explicitly,
 * demonstrating awareness of thread-safety concerns required by R5.
 */
@Configuration
public class ExecutorConfig {

    @Value("${pipes.executor.core-pool-size}")
    private int corePoolSize;

    @Value("${pipes.executor.max-pool-size}")
    private int maxPoolSize;

    @Value("${pipes.executor.queue-capacity}")
    private int queueCapacity;

    /**
     * Named bean so it can be injected by type or name.
     * The executor is shared across all pipeline runs — jobs from different
     * runs compete for the same thread pool, simulating a real CI server.
     */
    @Bean(name = "pipelineExecutor", destroyMethod = "shutdown")
    public ExecutorService pipelineExecutor() {
        return new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity),
                r -> {
                    Thread t = new Thread(r);
                    t.setName("pipes-worker-" + t.getId());
                    t.setDaemon(true); // don't block JVM shutdown
                    return t;
                },
                new ThreadPoolExecutor.CallerRunsPolicy() // back-pressure: caller runs task if queue full
        );
    }
}
