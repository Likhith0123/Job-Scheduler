package com.chronoflow.job.repository;

import com.chronoflow.job.entity.JobRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobRunRepository extends JpaRepository<JobRun, UUID> {

    List<JobRun> findByJobIdOrderByStartedAtDesc(UUID jobId);

    Optional<JobRun> findByIdAndTenantId(UUID id, UUID tenantId);
}
