package com.sprint.team2.monew.domain.userActivity.events.commentEvent;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentUpdateEvent (
  UUID id, // comment id
  UUID articleId,
  String articleTitle,
  UUID userId,
  String userNickname,
  String content,
  long likeCount,
  LocalDateTime createdAt
){

}
