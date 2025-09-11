package com.sprint.team2.monew.domain.article.service;

import com.sprint.team2.monew.domain.article.dto.response.ArticleRestoreResultDto;
import java.time.LocalDateTime;

public interface ArticleStorageService {
  void backupToS3(String filename, String article);
  ArticleRestoreResultDto restoreArticle(LocalDateTime from, LocalDateTime to);
}
