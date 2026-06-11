package com.chronoflow.job.repository;

import com.chronoflow.job.entity.Job;
import com.chronoflow.job.entity.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobRepository extends JpaRepository<Job, UUID> {

    List<Job> findByTenantIdAndStatusNotOrderByCreatedAtDesc(UUID tenantId, JobStatus status);

    Optional<Job> findByIdAndTenantIdAndStatusNot(UUID id, UUID tenantId, JobStatus status);

    @Query("""
            SELECT j FROM Job j
            WHERE j.status = com.chronoflow.job.entity.JobStatus.ACTIVE
              AND j.nextRunAt IS NOT NULL
              AND j.nextRunAt <= :now
            ORDER BY j.nextRunAt ASC
            """)
    List<Job> findDueJobs(@Param("now") Instant now);
}
