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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
  @Transactional
  public ArticleRestoreResultDto restoreArticle(LocalDate from, LocalDate to) {
    String bucket = s3Properties.bucket();
    List<Article> allS3Articles = new ArrayList<>();

    log.info("[뉴스 기사 복구] DB에서 기간 내 모든 기사(삭제 포함)의 현재 상태를 조회합니다: {} ~ {}", from, to);
    Map<String, Article> dbArticleMap = articleRepository.findByPublishDateBetween(
            from.atStartOfDay(),
            to.plusDays(1).atStartOfDay()
        ).stream()
        .collect(Collectors.toMap(Article::getSourceUrl, Function.identity()));
    log.info("[뉴스 기사 복구] DB에서 {}개의 기존 기사 정보를 확인했습니다.", dbArticleMap.size());

    log.info("[뉴스 기사 복구] S3로부터 뉴스 기사 복구 시작. 기간: {} ~ {}", from, to);

    // from - to 날짜를 하루씩 루프
    for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
      String prefix = String.format(
          "articles-%s/", // prod
//          "test-articles-%s/", // local
          date.format(DateTimeFormatter.ISO_LOCAL_DATE)
      );

      log.info("[뉴스 기사 복구] S3 디렉토리 처리 중: {}", prefix);
      ListObjectsV2Request listReq = ListObjectsV2Request.builder()
          .bucket(bucket)
          .prefix(prefix)
          .build();
      ListObjectsV2Response listRes = s3Client.listObjectsV2(listReq);

      for (S3Object summary : listRes.contents()) {
        String chunkFileKey = summary.key();
        log.info("[뉴스 기사 복구] 청크 파일 읽는 중: {}", chunkFileKey);

        GetObjectRequest getReq = GetObjectRequest.builder()
            .bucket(bucket)
            .key(chunkFileKey)
            .build();

        // 현재 날짜에 있는 모든 오브젝트를 리스트로 받아오고 allS3Articles에 추가
        try (ResponseInputStream<GetObjectResponse> s3ObjectStream = s3Client.getObject(getReq)) {
          List<Article> articlesFromChunk = objectMapper.readValue(
              s3ObjectStream,
              new TypeReference<>() {}
          );
          if (articlesFromChunk != null && !articlesFromChunk.isEmpty()) {
            allS3Articles.addAll(articlesFromChunk);
          }
        } catch (S3Exception | IOException e) {
          log.error("[뉴스 기사 복구] S3 prefix에서 파일 읽는 중 오류 발생. Prefix: {}", prefix, e);
        }
      }
    } // S3 루프 끝

    Collection<Article> uniqueS3Articles = allS3Articles.stream()
        .collect(Collectors.toMap(
            Article::getSourceUrl,      // Key: 중복 기준이 될 sourceUrl
            article -> article,         // Value: 기사 객체 자체
            (existingValue, newValue) -> existingValue // 만약 Key가 중복되면, 기존 값을 유지
        ))
        .values();

    List<Article> articlesToUndelete = new ArrayList<>(); // soft delete된 기사들
    List<Article> articlesToInsert = new ArrayList<>(); // hard delete된 기사들

    for (Article s3Article : uniqueS3Articles) {
      // 현재 db에서 가져온 기사
      Article dbArticle = dbArticleMap.get(s3Article.getSourceUrl());

      if (dbArticle == null) {
        // hard-deleted -> INSERT
        s3Article.setIdToNull();
        articlesToInsert.add(s3Article);
      } else if (dbArticle.getDeletedAt() != null) {
        // soft-deleted (deletedAt != null) -> undelete
        dbArticle.setDeletedAt(null);
        articlesToUndelete.add(dbArticle);
      }
    }

    if (articlesToInsert.isEmpty() && articlesToUndelete.isEmpty()) {
      log.info("[뉴스 기사 복구] 유실된 기사가 없어 복구를 종료합니다.");
      return new ArticleRestoreResultDto(LocalDateTime.now(), new ArrayList<>(), 0);
    }

    if (!articlesToUndelete.isEmpty()) {
      List<Article> uniqueUndeletes = new ArrayList<>(new LinkedHashSet<>(articlesToUndelete));
      articleRepository.saveAll(uniqueUndeletes);
      log.info("[뉴스 기사 복구] 총 {}개의 소프트 삭제된 기사를 복구했습니다.", uniqueUndeletes.size());
    }
    if (!articlesToInsert.isEmpty()) {
      articleRepository.saveAll(articlesToInsert);
      log.info("[뉴스 기사 복구] 총 {}개의 새로운 기사를 DB에 저장했습니다.", articlesToInsert.size());
    }

    List<UUID> savedArticleIdsTotal = Stream.concat(
            articlesToInsert.stream().map(Article::getId),
            articlesToUndelete.stream().map(Article::getId)
        )
        .collect(Collectors.toList());

    log.info("[뉴스 기사 복구] 총 {}개의 뉴스 기사 복구 완료. 기간: {} ~ {}", savedArticleIdsTotal.size(), from, to);
    return new ArticleRestoreResultDto(
        LocalDateTime.now(),
        savedArticleIdsTotal,
        savedArticleIdsTotal.size()
    );
  }
}

