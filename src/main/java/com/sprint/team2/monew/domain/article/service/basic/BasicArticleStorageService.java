package com.sprint.team2.monew.domain.article.service.basic;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.team2.monew.domain.article.dto.response.ArticleRestoreResultDto;
import com.sprint.team2.monew.domain.article.entity.Article;
import com.sprint.team2.monew.domain.article.repository.ArticleRepository;
import com.sprint.team2.monew.domain.article.service.ArticleStorageService;
import com.sprint.team2.monew.domain.base.BaseEntity;
import com.sprint.team2.monew.global.config.aws.S3Properties;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicArticleStorageService implements ArticleStorageService {

  private final S3Client s3Client;
  private final S3Properties s3Properties;
  private final ArticleRepository articleRepository;
  private final ObjectMapper objectMapper;

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
  public ArticleRestoreResultDto restoreArticle(LocalDate from, LocalDate to) {
    String bucket = s3Properties.bucket();
    List<Article> missingArticlesTotal = new ArrayList<>();

    // 날짜 사이에 있는 모든 Article url fetch (non-deleted)
    Set<String> existingArticleUrls = articleRepository.findArticleUrlsBetweenDates(
        from.atStartOfDay(), // 하루 시작
        to.plusDays(1).atStartOfDay() // 다음날 하루 시작
    );
    log.info("[뉴스 기사 복구] 기간: {} ~ {}. repository에 저장된 기사 url들: {}",
        from, to, existingArticleUrls);
    log.info("[뉴스 기사 복구] S3로부터 뉴스 기사 복구 시작. 기간: {} ~ {}", from, to);

    // from, to 사이에 있는 날짜 루프
    for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
      List<Article> articlesFromS3 = new ArrayList<>();
      String prefix = String.format(
          "test-articles-%s/", // local
//          "articles-%s/", // prod
          date.format(DateTimeFormatter.ISO_LOCAL_DATE)
      );

      log.info("[뉴스 기사 복구] S3 디렉토리 처리 중: {}", prefix);
      ListObjectsV2Request listReq = ListObjectsV2Request.builder()
          .bucket(bucket)
          .prefix(prefix)
          .build();
      ListObjectsV2Response listRes = s3Client.listObjectsV2(listReq);

      // 현재 날짜 디렉토리에 있는 파일들 루프
      for (S3Object summary : listRes.contents()) {
        String chunkFileKey = summary.key();
        log.info("[뉴스 기사 복구] 청크 파일 읽는 중: {}", chunkFileKey);

        GetObjectRequest getReq = GetObjectRequest.builder()
            .bucket(bucket)
            .key(chunkFileKey)
            .build();

        try (ResponseInputStream<GetObjectResponse> s3ObjectStream = s3Client.getObject(getReq)) {
          List<Article> articlesFromChunk = objectMapper.readValue(
              s3ObjectStream,
              new TypeReference<>() {
              }
          );
          if (articlesFromChunk != null && !articlesFromChunk.isEmpty()) {
            articlesFromS3.addAll(articlesFromChunk);
          }
        } catch (S3Exception | IOException e) {
          log.error("[뉴스 기사 복구] S3 prefix에서 파일 읽는 중 오류 발생. Prefix: {}", prefix, e);
        }
      }

      // DB에 있는거 필터링
      List<Article> missingArticles = articlesFromS3.stream()
          .filter(article -> !existingArticleUrls.contains(article.getSourceUrl()))
          .toList();

      if (!missingArticles.isEmpty()) {
        log.info("[뉴스 기사 복구] 날짜 {}에 {}개의 유실된 기사를 찾았습니다.", date, missingArticles.size());
        missingArticlesTotal.addAll(missingArticles);
      }
    } // 루프 끝

    if (missingArticlesTotal.isEmpty()) {
      log.info("[뉴스 기사 복구] 유실된 기사가 없어 복구를 종료합니다.");
      return new ArticleRestoreResultDto(LocalDateTime.now(), new ArrayList<>(), 0);
    }

//    missingArticlesTotal.forEach(Article::setIdToNull);

    List<Article> articlesToUndelete = new ArrayList<>();
    List<Article> articlesToInsert = new ArrayList<>();

    for (Article a : missingArticlesTotal) {
      // soft delete
      if (a.getDeletedAt() != null) { // if by the time of backup, the article is soft deleted
        a.setDeletedAt(null);
        articlesToUndelete.add(a);
      } else { // hard delete
        a.setIdToNull();
        articlesToInsert.add(a);
      }
    }

    if (!articlesToUndelete.isEmpty()) { //update
      articleRepository.saveAll(articlesToUndelete);
      log.info("[뉴스 기사 복구] 총 {}개의 소프트 삭제된 기사를 복구했습니다.", articlesToUndelete.size());
    }
    if (!articlesToInsert.isEmpty()) { //insert
      articleRepository.saveAll(articlesToInsert);
      log.info("[뉴스 기사 복구] 총 {}개의 새로운 기사를 DB에 저장했습니다.", articlesToInsert.size());
    }

//    articleRepository.saveAll(missingArticlesTotal);

    log.info("[뉴스 기사 복구] 총 {}개의 뉴스 기사를 DB에 저장했습니다.", missingArticlesTotal.size());
//    List<UUID> savedArticleIdsTotal = missingArticlesTotal.stream()
//        .map(BaseEntity::getId)
//        .toList();

    List<UUID> savedArticleIdsTotal = Stream.concat(
            articlesToInsert.stream().map(Article::getId),
            articlesToUndelete.stream().map(Article::getId)
        )
        .collect(Collectors.toList());

    log.info("[뉴스 기사 복구] 총 {}개의 뉴스 기사 복구 완료.기간: {} ~ {}", savedArticleIdsTotal.size(), from, to);
    return new ArticleRestoreResultDto(
        LocalDateTime.now(),
        savedArticleIdsTotal,
        savedArticleIdsTotal.size()
    );
  }
}

