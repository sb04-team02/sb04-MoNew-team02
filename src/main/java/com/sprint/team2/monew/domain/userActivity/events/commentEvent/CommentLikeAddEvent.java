package com.sprint.team2.monew.domain.userActivity.events.commentEvent;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public record CommentLikeAddEvent (
  UUID id,
  LocalDateTime createdAt,
  UUID commentId,
  UUID articleId,
  String articleTitle,
  UUID commentUserId,
  String commentUserNickname,
  String commentContent,
  long commentLikeCount,
  LocalDateTime commentCreatedAt
){

}
