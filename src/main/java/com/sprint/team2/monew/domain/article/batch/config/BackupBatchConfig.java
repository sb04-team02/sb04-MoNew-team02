package com.sprint.team2.monew.domain.article.batch.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.team2.monew.domain.article.entity.Article;
import com.sprint.team2.monew.domain.article.service.ArticleStorageService;
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class BackupBatchConfig {

  private final ArticleStorageService articleStorageService;
  private final ObjectMapper objectMapper;
  private final int chunkSize = 100;

  // ===================== Job =====================
  @Bean(name="backupNewsJob")
  public Job backupNewsJob(
      JobRepository jobRepository,
      Step newsBackupStep
  ) {
    return new JobBuilder("backupNewsJob", jobRepository)
        .start(newsBackupStep)
        .build();
  }

  // ===================== Step (Job에서 사용) =====================
  @Bean
  public Step newsBackupStep(
      JpaPagingItemReader<Article> newsBackupReader,
      ItemProcessor<Article, Article> newsBackupProcessor,
      ItemWriter<Article> newsBackupWriter,
      JobRepository jobRepository,
      PlatformTransactionManager transactionManager
  ) {
    return new StepBuilder("newsBackupStep", jobRepository)
        .<Article, Article>chunk(chunkSize, transactionManager)
        .reader(newsBackupReader)
        .processor(newsBackupProcessor)
        .writer(newsBackupWriter)
        .build();
  }

  // ===================== ItemReader, ItemProcessor, ItemWriter =====================

  // Reader - 특정 날짜의 Article 객체를 데이터베이스에서 읽음
  @Bean
  @StepScope
  public JpaPagingItemReader<Article> newsBackupReader(
      EntityManagerFactory entityManagerFactory,
      @Value("#{jobParameters['backupDate']}") String backupDateStr
  ) {
    LocalDate backupDate = LocalDate.parse(backupDateStr);

    Map<String, Object> parameters = new HashMap<>();
    parameters.put("startDate", backupDate.atStartOfDay());
    parameters.put("endDate", backupDate.plusDays(1).atStartOfDay());

    return new JpaPagingItemReaderBuilder<Article>()
        .name("articleJpaPagingItemReader")
        .entityManagerFactory(entityManagerFactory)
//        .queryString("SELECT a FROM Article a "
//            + "WHERE a.createdAt >= :startDate AND a.createdAt < :endDate")
        .queryString("SELECT a FROM Article a "
            + "WHERE a.publishDate >= :startDate AND a.publishDate < :endDate")
        .parameterValues(parameters)
        .pageSize(chunkSize)
        .build();
  }

  // Processor - 각 Article 객체를 JSON으로 변환
  @Bean
  public ItemProcessor<Article, Article> newsBackupProcessor() {
//    return objectMapper::writeValueAsString;
    return article -> article;
  }

  // Writer - 변환된 데이터(JSON)를 AWS S3에 파일로 기록 (배치 방식으로 S3에 백업)
  @Bean
  @StepScope
  public ItemWriter<Article> newsBackupWriter(
      @Value("#{jobParameters['backupDate']}") String backupDateStr
  ) {
    return articles -> {
      log.info("[뉴스 기사 백업 (배치)] 날짜 {}: 기사 {}개",
          backupDateStr, articles.getItems().size());

      if (articles.getItems().isEmpty()) {
        return;
      }

      /** 저장되는 포맷:
       * [
       *   {"id": "...", "title": "첫 번째 기사"},
       *   {"id": "...", "title": "두 번째 기사"},
       *   {"id": "...", "title": "세 번째 기사"}
       * ]
       */
      String aggregatedJsonArray = objectMapper.writeValueAsString(articles.getItems());

      String filename = String.format(
//          "test-articles-%s/chunk-%s.json", // local
          "articles-%s/chunk-%s.json", // test
          backupDateStr,
          UUID.randomUUID()
          );
      articleStorageService.backupToS3(filename, aggregatedJsonArray);
    };
  }

}
