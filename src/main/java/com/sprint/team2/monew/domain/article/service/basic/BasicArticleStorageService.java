package com.sprint.team2.monew.domain.article.service.basic;

import com.sprint.team2.monew.domain.article.dto.response.ArticleRestoreResultDto;
import com.sprint.team2.monew.domain.article.entity.Article;
import com.sprint.team2.monew.domain.article.repository.ArticleRepository;
import com.sprint.team2.monew.domain.article.service.ArticleStorageService;
import com.sprint.team2.monew.domain.base.BaseEntity;
import com.sprint.team2.monew.global.config.aws.S3Properties;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import javax.inject.Qualifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicArticleStorageService implements ArticleStorageService {

  private final S3Client s3Client;
  private final S3Properties s3Properties;
  private final JobLauncher jobLauncher;
  private final Job restoreNewsJob;
  private final ArticleRepository articleRepository;

  @Override
  public void backupToS3(String filename, String aggregateJson){

    try {
      log.info("[뉴스 기사] 백업 시작 - filename ={}", filename);

      String contentType = "application/json";
      String bucket = s3Properties.bucket();

      PutObjectRequest putObjectRequest = PutObjectRequest.builder()
          .bucket(bucket)
          .key(filename)
          .contentType(contentType)
          .build();

      s3Client.putObject(
          putObjectRequest,
          RequestBody.fromString(aggregateJson)
      );
      log.info("[뉴스 기사] 백업 완료 - filename ={}", filename);


    } catch (S3Exception e) {
      log.error("[뉴스 기사] 백업 오류 (S3) : {}", e.awsErrorDetails().errorMessage());

    } catch (Exception e) {
      log.error("[뉴스 기사] 백업 오류 : ", e);
    }
  }

  @Override
  public ArticleRestoreResultDto restoreArticle(LocalDateTime from, LocalDateTime to) {
    try {
      log.info("[뉴스 기사] 뉴스 기사 복구 배치 실행 시작");
      String backupDate = LocalDate.now().minusDays(1)
          .format(DateTimeFormatter.ISO_LOCAL_DATE);

      JobParameters params = new JobParametersBuilder()
          .addString("backupDate", backupDate)
          .addLong("timestamp", System.currentTimeMillis())
          .toJobParameters();

      JobExecution jobExecution = jobLauncher.run(restoreNewsJob, params);

      if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
        Collection<StepExecution> stepExecutions = jobExecution.getStepExecutions();
        StepExecution restoreStepExecution = stepExecutions.iterator().next();

        List<Article> articles = (List<Article>) restoreStepExecution.getExecutionContext().get("items");

        if (articles == null) {
          articles = new ArrayList<>();
        }

        // save to db
        articleRepository.saveAll(articles);

        List<UUID> articleIds = articles.stream()
            .map(BaseEntity::getId)
            .toList();

      log.info("[뉴스 기사] 뉴스 기사 복구 배치 실행 완료");
        return new ArticleRestoreResultDto(
            LocalDateTime.now(),
            articleIds,
            articles.size()
        );

      } else {
        log.error("[뉴스 기사] 복구 배치 실패. Status: {}", jobExecution.getStatus());
      }

    } catch (Exception e) {
      log.error("[뉴스 기사] 뉴스 기사 복구 배치 실행 실패", e);
    }
    return new ArticleRestoreResultDto(LocalDateTime.now(), new ArrayList<>(), 0);
  }

}

