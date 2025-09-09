package com.sprint.team2.monew.domain.article.service.basic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.team2.monew.domain.article.entity.Article;
import com.sprint.team2.monew.domain.article.service.ArticleBackupService;
import com.sprint.team2.monew.domain.article.service.S3Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicArticleBackupService implements ArticleBackupService {

  private final S3Client s3Client;
  private final S3Properties s3Properties;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void backupToS3(String articleJson){

    try {
      Article article = objectMapper.readValue(articleJson, Article.class);
      log.info("[뉴스 기사] 백업 시작 - articleId ={}", article.getId());

      String contentType = "application/json";
      String bucket = s3Properties.bucket();

      PutObjectRequest putObjectRequest = PutObjectRequest.builder()
          .bucket(bucket)
          .key(article.getId().toString() + ".json")
          .contentType(contentType)
          .build();

      s3Client.putObject(
          putObjectRequest,
          RequestBody.fromString(articleJson)
      );

    } catch (S3Exception e) {
      log.error("[뉴스 기사] 백업 오류 (S3) : {}", e.awsErrorDetails().errorMessage());

    } catch (Exception e) {
      log.error("[뉴스 기사] 백업 오류 : ", e);
    }
  }
}
