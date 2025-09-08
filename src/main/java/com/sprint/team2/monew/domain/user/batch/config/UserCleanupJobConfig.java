package com.sprint.team2.monew.domain.user.batch.config;

import com.sprint.team2.monew.domain.user.batch.listener.UserCleanupJobListener;
import com.sprint.team2.monew.domain.user.service.UserService;
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

@Configuration
@RequiredArgsConstructor
public class UserCleanupJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final UserService userService;
    private final UserCleanupJobListener jobListener;

    @Bean
    public Job userCleanupJob() {
        return new JobBuilder("userCleanupJob", jobRepository)
                .listener(jobListener) // Listener 연결 (커스텀 메트릭)
                .start(userCleanupStep())
                .build();
    }

    @Bean
    public Step userCleanupStep() {
        return new StepBuilder("userCleanupStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    long deletedCount = userService.deletePhysicallyByBatch();

                    JobExecution jobExecution = contribution.getStepExecution().getJobExecution();
                    jobExecution.getExecutionContext().putLong("deletedCount", deletedCount);

                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}