package com.pipes.service;

import com.pipes.entity.PipelineRun;
import com.pipes.repository.PipelineRunRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RunPersistenceService {

    private final PipelineRunRepository runRepository;

    public RunPersistenceService(PipelineRunRepository runRepository) {
        this.runRepository = runRepository;
    }

    @Transactional
    public PipelineRun save(PipelineRun run) {
        return runRepository.saveAndFlush(run);
    }
}