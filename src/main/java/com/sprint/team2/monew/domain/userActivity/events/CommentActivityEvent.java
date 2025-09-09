package com.sprint.team2.monew.domain.userActivity.events;


import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;

@Getter
public class CommentActivityEvent {
  UUID id;
  UUID articleId;
  String articleTitle;
  UUID userId;
  String userNickname;
  String content;
  long likeCount;
  LocalDateTime createdAt;
}
