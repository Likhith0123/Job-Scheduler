package com.chronoflow.executor.service;

import com.chronoflow.common.ChronoFlowTopics;
import com.chronoflow.common.dto.JobRunResponse;
import com.chronoflow.common.event.JobCompletedEvent;
import com.chronoflow.common.event.JobDueEvent;
import com.chronoflow.executor.client.JobServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
public class JobExecutionService {

    private static final Logger log = LoggerFactory.getLogger(JobExecutionService.class);

    private final JobServiceClient jobServiceClient;
    private final KafkaTemplate<String, JobCompletedEvent> kafkaTemplate;
    private final long simulatedDurationMs;

    public JobExecutionService(JobServiceClient jobServiceClient,
                               KafkaTemplate<String, JobCompletedEvent> kafkaTemplate,
                               @Value("${chronoflow.executor.simulated-duration-ms:500}") long simulatedDurationMs) {
        this.jobServiceClient = jobServiceClient;
        this.kafkaTemplate = kafkaTemplate;
        this.simulatedDurationMs = simulatedDurationMs;
    }

    public void execute(JobDueEvent event) {
        JobRunResponse run = jobServiceClient.startRun(event.jobId(), event.tenantId());
        log.info("Started run {} for job {} ({})", run.id(), event.jobId(), event.jobName());

        try {
            simulateWork(event.payload());
            String result = "Executed payload for job " + event.jobName();
            JobRunResponse completed = jobServiceClient.completeRun(
                    run.id(),
                    event.tenantId(),
                    "SUCCEEDED",
                    result
            );
            publishCompleted(completed, event);
            log.info("Completed run {} for job {}", run.id(), event.jobId());
        } catch (Exception ex) {
            log.error("Failed run {} for job {}", run.id(), event.jobId(), ex);
            JobRunResponse failed = jobServiceClient.completeRun(
                    run.id(),
                    event.tenantId(),
                    "FAILED",
                    ex.getMessage()
            );
            publishCompleted(failed, event);
        }
    }

    private void simulateWork(Map<String, Object> payload) throws InterruptedException {
        log.debug("Executing payload: {}", payload);
        Thread.sleep(simulatedDurationMs);
    }

    private void publishCompleted(JobRunResponse run, JobDueEvent sourceEvent) {
        JobCompletedEvent event = new JobCompletedEvent(
                run.id(),
                sourceEvent.jobId(),
                sourceEvent.tenantId(),
                run.status(),
                run.result(),
                Instant.now()
        );
        kafkaTemplate.send(ChronoFlowTopics.JOB_COMPLETED, run.id().toString(), event);
    }
}
