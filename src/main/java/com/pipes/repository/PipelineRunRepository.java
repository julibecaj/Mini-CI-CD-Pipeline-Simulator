package com.pipes.repository;

import com.pipes.entity.Pipeline;
import com.pipes.entity.PipelineRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Spring Data JPA repository for PipelineRun (R7). */
@Repository
public interface PipelineRunRepository extends JpaRepository<PipelineRun, Long> {

    List<PipelineRun> findByPipelineOrderByStartedAtDesc(Pipeline pipeline);

    List<PipelineRun> findByPipeline_Owner_UsernameOrderByStartedAtDesc(String username);

    long countByPipelineAndStatus(Pipeline pipeline, PipelineRun.RunStatus status);
}
