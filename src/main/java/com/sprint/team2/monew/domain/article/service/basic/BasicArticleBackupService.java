package com.sprint.team2.monew.domain.article.service.basic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.team2.monew.domain.article.entity.Article;
import com.sprint.team2.monew.domain.article.service.ArticleBackupService;
import com.sprint.team2.monew.global.config.aws.S3Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicArticleBackupService implements ArticleBackupService {

  private final S3Client s3Client;
  private final S3Properties s3Properties;

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
}
