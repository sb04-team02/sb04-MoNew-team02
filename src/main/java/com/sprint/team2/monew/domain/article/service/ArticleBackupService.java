package com.sprint.team2.monew.domain.article.service;

import com.sprint.team2.monew.domain.article.entity.Article;

public interface ArticleBackupService {
  void backupToS3(String filename, String article);
}
