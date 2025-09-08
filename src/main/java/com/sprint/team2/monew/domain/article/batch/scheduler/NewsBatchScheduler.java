package com.sprint.team2.monew.domain.article.batch.scheduler;

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
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class NewsBatchScheduler {
    private final JobLauncher jobLauncher;
    private final Job newsCollectJob;

    @Scheduled(cron = "0 0 * * * *")
    public void runNewsCollectionJob() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(newsCollectJob, params);
            log.info("[Article] 뉴스 기사 수집 배치 실행 완료");
        } catch (Exception e) {
            log.error("[Article] 뉴스 기사 수집 배치 실행 실패", e);
        }
    }

}
