package com.sprint.team2.monew.domain.article.batch.config;

import com.sprint.team2.monew.domain.article.entity.Article;
import com.sprint.team2.monew.domain.article.repository.ArticleRepository;
import com.sprint.team2.monew.global.config.aws.S3Properties;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class RestoreBatchConfig {

  private final ArticleRepository articleRepository;
  private final S3Properties s3Properties;
  @Qualifier("webApplicationContext")
  private final ResourcePatternResolver resourcePatternResolver;
  private final int chunkSize = 100;

  @Bean(name="restoreNewsJob")
  public Job restoreNewsJob(
      JobRepository jobRepository,
      Step newsRestoreStep
  ) {
    return new JobBuilder("restoreNewsJob", jobRepository)
        .start(newsRestoreStep)
        .build();
  }

  // ===================== Step (used in Job) =====================
  @Bean
  public Step newsRestoreStep(
      MultiResourceItemReader<Article> newsRestoreReader,
      ItemProcessor<Article, Article> newsRestoreProcessor,
      JobRepository jobRepository,
      ItemWriter<Article> newsRestoreWriter,
      PlatformTransactionManager transactionManager
  ) {
    return new StepBuilder("newsRestoreStep", jobRepository)
        .<Article, Article> chunk(chunkSize, transactionManager)
        .reader(newsRestoreReader)
        .processor(newsRestoreProcessor)
        .writer(newsRestoreWriter)
        .build();
  }

  // ===================== ItemReader, ItemProcessor, ItemWriter =====================
  // Reader - Reads Article objects from S3 for a specific date
  @Bean
  @StepScope
  public MultiResourceItemReader<Article> newsRestoreReader(
      @Value("#{jobParameters['backupDate']}") String restoreDate
  ) {

    String s3PathPattern = String.format("s3://%s/articles-%s/chunk-*.json",
        s3Properties.bucket(),
        restoreDate);

    try {
      MultiResourceItemReader<Article> resourceItemReader = new MultiResourceItemReader<>();
      Resource[] resources = resourcePatternResolver.getResources(s3PathPattern);
      resourceItemReader.setResources(resources);
      resourceItemReader.setDelegate(singleJsonFileReader());
      return resourceItemReader ;
    } catch (Exception e) {
      // custom error로 대체하기
      throw new RuntimeException("Failed to find S3 backup files for date: " + restoreDate, e);
    }
  }

  // *Helper bean - defines how to read a single JSON file (delegate for MultiResourceItemReader)
  @Bean
  public JsonItemReader<Article> singleJsonFileReader() {
    return new JsonItemReaderBuilder<Article>()
        .name("jsonArticleReader")
        .jsonObjectReader(new JacksonJsonObjectReader<>(Article.class))
        .build();
  }

  // Processor - only return if it doesn't exist in current DB
  @Bean
  public ItemProcessor<Article, Article> newsRestoreProcessor() {
    return article -> articleRepository.existsById(article.getId()) ? null : article;
  }

  // Writer - Saves all restored files in repository
  @Bean
  @StepScope
  public ItemWriter<Article> newsRestoreWriter() {

    return new ItemWriter<Article>() {

      private StepExecution stepExecution;

      @BeforeStep
      public void saveStepExecution(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
        this.stepExecution.getExecutionContext().put("items", new ArrayList<>());
//        return articles -> articleRepository.saveAll(articles);
//        return articleRepository::saveAll;
      }

      @SuppressWarnings("unchecked")
      @Override
      public void write(Chunk<? extends Article> chunk) throws Exception {
        // get list of articles to be written from chunk
        List<? extends Article> savedArticles = chunk.getItems();

        // save to db
        articleRepository.saveAll(savedArticles);

        // getting existing list from execution context & adding the saved articles
        List<Article> itemsList = (List<Article>) stepExecution.getExecutionContext().get("items"); // empty
        itemsList.addAll(savedArticles);
        // put the updated list back into the execution context
        // needed for retrieving it after the job is done.
        this.stepExecution.getExecutionContext().put("items", itemsList);

      }
    };
  }
}



