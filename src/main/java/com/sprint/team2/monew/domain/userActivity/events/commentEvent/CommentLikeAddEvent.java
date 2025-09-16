package com.sprint.team2.monew.domain.userActivity.events.commentEvent;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public record CommentLikeAddEvent (
  UUID id, // reaction Id (좋아요 id)
  LocalDateTime createdAt,
  UUID commentId,
  UUID articleId,
  String articleTitle,
  UUID commentUserId, // 댓글 작성자
  String commentUserNickname,
  String commentContent,
  long commentLikeCount,
  LocalDateTime commentCreatedAt
){

}
