package com.sprint.team2.monew.domain.article.batch.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.team2.monew.domain.article.entity.Article;
import com.sprint.team2.monew.domain.article.repository.ArticleRepository;
import com.sprint.team2.monew.domain.article.service.ArticleBackupService;
import jakarta.persistence.EntityManagerFactory;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class BackupBatchConfig {

  private final ArticleBackupService articleBackupService;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final int chunkSize = 100;

  // ===================== Job =====================
  @Bean
  public Job backupNewsJob(
      JobRepository jobRepository,
      Step newsBackupStep
  ) {
    return new JobBuilder("backupNewsJob", jobRepository)
        .start(newsBackupStep)
        .build();
  }

  // ===================== Step (used in Job) =====================
  @Bean
  public Step newsBackupStep(
      JpaPagingItemReader<Article> newsBackupReader,
      ItemProcessor<Article, String> newsBackupProcessor,
      ItemWriter<String> newsBackupWriter,
      JobRepository jobRepository,
      PlatformTransactionManager transactionManager
  ) {
    return new StepBuilder("newsBackupStep", jobRepository)
        .<Article, String>chunk(chunkSize, transactionManager)
        .reader(newsBackupReader)
        .processor(newsBackupProcessor)
        .writer(newsBackupWriter)
        .build();

  }

  // ===================== Components in Step: ItemReader, ItemProcessor, ItemWriter =====================

  // Reader - Reads Article objects from the database for a specific date
  @Bean
  public JpaPagingItemReader<Article> newsBackupReader(EntityManagerFactory entityManagerFactory) {
    return new JpaPagingItemReaderBuilder<Article>()
        .name("articleJpaPagingItemReader")
        .entityManagerFactory(entityManagerFactory)
        .queryString("SELECT a FROM Article a")
        .pageSize(chunkSize)
        .build();
  }

  // Processor - Transforms each Article object into JSON
  @Bean
  public ItemProcessor<Article, String> newsBackupProcessor() {
    // return article -> objectMapper.writeValueAsString(article)
    return objectMapper::writeValueAsString;
  }

  // Writer - Writes the processed data (JSON) to a file in AWS S3 (배치로 S3에 백업)
  @Bean
  public ItemWriter<String> newsBackupWriter() {
    return articles -> {
      String aggregatedJson = String.join("\n", articles.getItems());
      String filename = "backup-" + UUID.randomUUID() + ".json";
      articleBackupService.backupToS3(filename, aggregatedJson);
    };
  }

}
