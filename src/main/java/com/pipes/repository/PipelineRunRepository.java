package com.pipes.repository;

import com.pipes.entity.Pipeline;
import com.pipes.entity.PipelineRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

/** Spring Data JPA repository for PipelineRun (R7). */
@Repository
public interface PipelineRunRepository extends JpaRepository<PipelineRun, Long> {

    List<PipelineRun> findByPipelineOrderByStartedAtDesc(Pipeline pipeline);

    List<PipelineRun> findByPipeline_Owner_UsernameOrderByStartedAtDesc(String username);

    long countByPipelineAndStatus(Pipeline pipeline, PipelineRun.RunStatus status);

    @Query("""
    SELECT DISTINCT r
    FROM PipelineRun r
    LEFT JOIN FETCH r.pipeline p
    LEFT JOIN FETCH p.stages s
    LEFT JOIN FETCH s.jobs
    LEFT JOIN FETCH r.stageResults sr
    LEFT JOIN FETCH sr.jobResults
    WHERE r.id = :runId
""")
    Optional<PipelineRun> findByIdWithFullGraph(@Param("runId") Long runId);


}
