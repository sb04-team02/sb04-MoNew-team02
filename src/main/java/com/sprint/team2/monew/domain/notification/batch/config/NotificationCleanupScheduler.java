package com.sprint.team2.monew.domain.notification.batch.config;

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
public class NotificationCleanupScheduler {
    private final JobLauncher jobLauncher;
    private final Job notificationCleanupJob;

    @Scheduled(cron = "0 0 3 * * *")
    public void runJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis()) //중복 방지용
                    .toJobParameters();

            jobLauncher.run(notificationCleanupJob, jobParameters);
            log.info("[알림] 확인된 알림 삭제 배치 실행 완료");
        } catch (Exception e) {
            log.error("[알림] 확인된 알림 삭제 배치 실행 실패", e);
        }
    }
}
