package com.sprint.team2.monew.domain.notification.batch.config;

import com.sprint.team2.monew.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
public class NotificationCleanupJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final NotificationRepository notificationRepository;

    public static final String JOB_NAME = "notificationCleanupJob";

    @Bean
    public Job notificationCleanupJob() {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(notificationCleanupStep())
                .build();
    }

    @Bean
    public Step notificationCleanupStep() {
        return new StepBuilder("notificationCleanupStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    var oldNotifications = notificationRepository
                            .findAllByConfirmedIsTrueAndUpdatedAtBefore(LocalDateTime.now().minusDays(7));

                    notificationRepository.deleteAllInBatch(oldNotifications);

                    JobExecution jobExecution = contribution.getStepExecution().getJobExecution();
                    jobExecution.getExecutionContext().putLong("oldNotifications", oldNotifications.size());

                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}