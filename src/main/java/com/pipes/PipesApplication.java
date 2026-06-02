package com.pipes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Pipes — CI/CD Pipeline Simulator
 * Entry point for the Spring Boot application.
 *
 * @EnableAsync enables Spring's asynchronous method execution capability,
 * complementing the ExecutorService used in PipelineExecutorService (R5).
 */

@SpringBootApplication
@EnableAsync
public class PipesApplication {

    public static void main(String[] args) {

        SpringApplication.run(PipesApplication.class, args);
    }

}


