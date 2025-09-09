package com.sprint.team2.monew.domain.article.batch.config;

import com.sprint.team2.monew.domain.article.entity.Article;
import com.sprint.team2.monew.domain.article.service.ArticleBackupService;
import com.sprint.team2.monew.domain.interest.entity.Interest;
import com.sprint.team2.monew.domain.interest.repository.InterestRepository;
import java.util.Iterator;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class BackupBatchConfig {

  private final InterestRepository interestRepository;
  private final ArticleBackupService articleBackupService;

  @Bean
  public Step newsBackupStep() {

  }

  // ===================== Components =====================

  // Reader: Reads Article objects from the database for a specific date
  @Bean
  public ItemReader<Article> newsBackupReader(InterestRepository interestRepository) {
    return new ItemReader<Article>() {

      private Iterator<Article> iterator;

      @Override
      public Article read() {
      }

    };
  }

  // Processor: Transforms each Article object into JSON
  @Bean
  public ItemProcessor<Article, String> newsBackupProcessor() {
    return
  }

  // Writer: Writes the processed data (JSON) to a file in AWS S3
  @Bean
  public ItemWriter<String> newsBackupWriter() {
    return articles -> {
      for (String article : articles) {
        articleBackupService.backupToS3(article);
      }
    };
  }

}
