package com.sprint.team2.monew.domain.article.batch.scheduler;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class TestJobRunner implements CommandLineRunner {

  private final NewsBackupBatchScheduler scheduler;

  public TestJobRunner(NewsBackupBatchScheduler scheduler) {
    this.scheduler = scheduler;
  }

  @Override
  public void run(String... args) throws Exception {
    scheduler.runBackupNewsSchedulerJob();
  }
}