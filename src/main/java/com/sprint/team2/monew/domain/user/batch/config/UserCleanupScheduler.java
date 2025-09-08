package com.sprint.team2.monew.domain.user.batch.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserCleanupScheduler {

    private final JobLauncher jobLauncher;
    private final Job userCleanupJob;

    @Scheduled(cron = "0 0 3 * * *") // 매일 새벽 3시 실행
    public void runJob() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis()) // 중복 실행 방지
                .toJobParameters();
        jobLauncher.run(userCleanupJob, params);
    }
}
