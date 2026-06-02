package com.pipes.repository;

import com.pipes.entity.Pipeline;
import com.pipes.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Spring Data JPA repository for Pipeline (R7). */
@Repository
public interface PipelineRepository extends JpaRepository<Pipeline, Long> {

    List<Pipeline> findByOwnerOrderByUpdatedAtDesc(User owner);

    Optional<Pipeline> findByIdAndOwner(Long id, User owner);

    @Query("SELECT COUNT(p) FROM Pipeline p WHERE p.owner = :owner")
    long countByOwner(User owner);
}
