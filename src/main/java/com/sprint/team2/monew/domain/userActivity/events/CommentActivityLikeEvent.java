package com.sprint.team2.monew.domain.userActivity.events;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;

@Getter
public class CommentActivityLikeEvent {
  UUID id;
  LocalDateTime createdAt;
  UUID commentId;
  UUID articleId;
  String articleTitle;
  UUID commentUserId;
  String commentUserNickname;
  String commentContent;
  long commentLikeCount;
  LocalDateTime commentCreatedAt;

}
