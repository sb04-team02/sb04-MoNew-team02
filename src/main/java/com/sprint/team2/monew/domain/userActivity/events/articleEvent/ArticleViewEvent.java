package com.sprint.team2.monew.domain.userActivity.events.articleEvent;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;

@Getter
public class ArticleViewEvent {
  UUID id; // userId
  UUID viewedBy;
  LocalDateTime createdAt;
  UUID articleId;
  String source;
  String sourceUrl;
  String articleTitle;
  LocalDateTime articlePublishedDate;
  String articleSummary;
  long articleCommentCount;
  long articleViewCount;
}
