package com.chronoflow.executor.kafka;

import com.chronoflow.common.ChronoFlowTopics;
import com.chronoflow.common.event.JobDueEvent;
import com.chronoflow.executor.service.JobExecutionService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class JobDueConsumer {

    private final JobExecutionService jobExecutionService;

    public JobDueConsumer(JobExecutionService jobExecutionService) {
        this.jobExecutionService = jobExecutionService;
    }

    @KafkaListener(topics = ChronoFlowTopics.JOB_DUE, containerFactory = "jobDueListenerFactory")
    public void onJobDue(JobDueEvent event) {
        jobExecutionService.execute(event);
    }
}
