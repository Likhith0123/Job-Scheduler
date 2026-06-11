package com.chronoflow.scheduler.service;

import com.chronoflow.common.ChronoFlowTopics;
import com.chronoflow.common.dto.JobResponse;
import com.chronoflow.common.event.JobDueEvent;
import com.chronoflow.scheduler.client.JobServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class JobDispatchService {

    private static final Logger log = LoggerFactory.getLogger(JobDispatchService.class);

    private final JobServiceClient jobServiceClient;
    private final KafkaTemplate<String, JobDueEvent> kafkaTemplate;

    public JobDispatchService(JobServiceClient jobServiceClient,
                              KafkaTemplate<String, JobDueEvent> kafkaTemplate) {
        this.jobServiceClient = jobServiceClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    public int dispatchDueJobs() {
        List<JobResponse> dueJobs = jobServiceClient.fetchDueJobs();
        int dispatched = 0;

        for (JobResponse job : dueJobs) {
            JobDueEvent event = new JobDueEvent(
                    job.id(),
                    job.tenantId(),
                    job.name(),
                    job.payload(),
                    Instant.now()
            );
            kafkaTemplate.send(ChronoFlowTopics.JOB_DUE, job.id().toString(), event);
            jobServiceClient.markScheduled(job.id());
            dispatched++;
            log.info("Dispatched job {} ({}) for tenant {}", job.id(), job.name(), job.tenantId());
        }

        return dispatched;
    }
}
