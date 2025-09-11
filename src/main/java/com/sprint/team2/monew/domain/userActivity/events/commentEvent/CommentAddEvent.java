package com.sprint.team2.monew.domain.userActivity.events.commentEvent;


import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CommentAddEvent {
  private final UUID id; // comment id
  private final UUID articleId;
  private final String articleTitle;
  private final UUID userId;
  private final String userNickname;
  private final String content;
  private final long likeCount;
  private final LocalDateTime createdAt;
}
