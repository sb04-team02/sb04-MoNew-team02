package com.sprint.team2.monew.domain.user.batch.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
public class UserCleanupScheduler {

    private final JobLauncher jobLauncher;
    private final Job userCleanupJob;

    @Scheduled(cron = "0 0 3 * * *") // 매일 새벽 3시 실행
    public void runJob() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis()) // 중복 실행 방지
                    .toJobParameters();
            jobLauncher.run(userCleanupJob, params);
            log.info("[사용자] 사용자 삭제 배치 처리 완료");
        } catch (Exception e) {
            log.error("[사용자] 사용자 삭제 배치 처리 실패", e);
        }
    }
}
