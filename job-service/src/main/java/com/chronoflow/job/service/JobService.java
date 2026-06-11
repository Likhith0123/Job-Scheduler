package com.chronoflow.job.service;

import com.chronoflow.common.dto.CreateJobRequest;
import com.chronoflow.common.dto.JobResponse;
import com.chronoflow.common.dto.JobRunResponse;
import com.chronoflow.job.entity.Job;
import com.chronoflow.job.entity.JobRun;
import com.chronoflow.job.entity.JobStatus;
import com.chronoflow.job.entity.RunStatus;
import com.chronoflow.job.repository.JobRepository;
import com.chronoflow.job.repository.JobRunRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final JobRunRepository jobRunRepository;
    private final CronService cronService;

    public JobService(JobRepository jobRepository,
                      JobRunRepository jobRunRepository,
                      CronService cronService) {
        this.jobRepository = jobRepository;
        this.jobRunRepository = jobRunRepository;
        this.cronService = cronService;
    }

    @Transactional
    public JobResponse createJob(UUID tenantId, CreateJobRequest request) {
        validateCron(request.cronExpression());
        Instant nextRun = cronService.nextRun(request.cronExpression(), Instant.now());
        Job job = jobRepository.save(new Job(
                tenantId,
                request.name(),
                request.cronExpression(),
                request.payload(),
                nextRun
        ));
        return toJobResponse(job);
    }

    @Transactional(readOnly = true)
    public List<JobResponse> listJobs(UUID tenantId) {
        return jobRepository.findByTenantIdAndStatusNotOrderByCreatedAtDesc(tenantId, JobStatus.DELETED)
                .stream()
                .map(this::toJobResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public JobResponse getJob(UUID tenantId, UUID jobId) {
        Job job = findJobForTenant(tenantId, jobId);
        return toJobResponse(job);
    }

    @Transactional
    public JobResponse pauseJob(UUID tenantId, UUID jobId) {
        Job job = findJobForTenant(tenantId, jobId);
        job.setStatus(JobStatus.PAUSED);
        return toJobResponse(job);
    }

    @Transactional
    public JobResponse resumeJob(UUID tenantId, UUID jobId) {
        Job job = findJobForTenant(tenantId, jobId);
        job.setStatus(JobStatus.ACTIVE);
        if (job.getNextRunAt() == null) {
            job.setNextRunAt(cronService.nextRun(job.getCronExpression(), Instant.now()));
        }
        return toJobResponse(job);
    }

    @Transactional
    public void deleteJob(UUID tenantId, UUID jobId) {
        Job job = findJobForTenant(tenantId, jobId);
        job.setStatus(JobStatus.DELETED);
    }

    @Transactional(readOnly = true)
    public List<JobRunResponse> listRuns(UUID tenantId, UUID jobId) {
        findJobForTenant(tenantId, jobId);
        return jobRunRepository.findByJobIdOrderByStartedAtDesc(jobId).stream()
                .map(this::toRunResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<JobResponse> findDueJobs() {
        return jobRepository.findDueJobs(Instant.now()).stream()
                .map(this::toJobResponse)
                .toList();
    }

    @Transactional
    public JobResponse markScheduled(UUID jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));
        job.setNextRunAt(cronService.nextRun(job.getCronExpression(), Instant.now()));
        return toJobResponse(job);
    }

    @Transactional
    public JobRunResponse startRun(UUID jobId, UUID tenantId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));
        if (!job.getTenantId().equals(tenantId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tenant mismatch");
        }
        return toRunResponse(jobRunRepository.save(new JobRun(jobId, tenantId)));
    }

    @Transactional
    public JobRunResponse completeRun(UUID runId, UUID tenantId, RunStatus status, String result) {
        JobRun run = jobRunRepository.findByIdAndTenantId(runId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Run not found"));
        run.setStatus(status);
        run.setResult(result);
        run.setCompletedAt(Instant.now());
        return toRunResponse(run);
    }

    private Job findJobForTenant(UUID tenantId, UUID jobId) {
        return jobRepository.findByIdAndTenantIdAndStatusNot(jobId, tenantId, JobStatus.DELETED)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));
    }

    private void validateCron(String cronExpression) {
        try {
            cronService.nextRun(cronExpression, Instant.now());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid cron expression");
        }
    }

    private JobResponse toJobResponse(Job job) {
        return new JobResponse(
                job.getId(),
                job.getTenantId(),
                job.getName(),
                job.getCronExpression(),
                job.getPayload(),
                job.getStatus().name(),
                job.getNextRunAt(),
                job.getCreatedAt()
        );
    }

    private JobRunResponse toRunResponse(JobRun run) {
        return new JobRunResponse(
                run.getId(),
                run.getJobId(),
                run.getTenantId(),
                run.getStatus().name(),
                run.getResult(),
                run.getStartedAt(),
                run.getCompletedAt()
        );
    }
}
