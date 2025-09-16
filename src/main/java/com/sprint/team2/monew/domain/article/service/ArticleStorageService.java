package com.sprint.team2.monew.domain.article.service;

import com.sprint.team2.monew.domain.article.dto.response.ArticleRestoreResultDto;
import java.time.LocalDate;

public interface ArticleStorageService {
  void backupToS3(String filename, String article);
  ArticleRestoreResultDto restoreArticle(LocalDate from, LocalDate to);
}
