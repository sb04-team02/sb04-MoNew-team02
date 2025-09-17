package com.sprint.team2.monew.domain.article.batch.scheduler;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
public class NewsBackupBatchScheduler {
  private final JobLauncher jobLauncher;
  private final Job backupNewsJob;

//  @Scheduled(cron = "0 0 0 * * *")
@Scheduled(cron = "0 * * * * *")
public void runBackupNewsSchedulerJob() {
    try {
      String backupDate = LocalDate.now().minusDays(1)
          .format(DateTimeFormatter.ISO_LOCAL_DATE);

      JobParameters params = new JobParametersBuilder()
          .addString("backupDate", backupDate)
          .addLong("timestamp", System.currentTimeMillis())
          .toJobParameters();

      jobLauncher.run(backupNewsJob, params);
      log.info("[뉴스 기사 백업] 뉴스 기사 백업 배치 실행 완료");
    } catch (Exception e) {
      log.error("[뉴스 기사 백업] 뉴스 기사 백업 배치 실행 실패", e);
    }
  }
}