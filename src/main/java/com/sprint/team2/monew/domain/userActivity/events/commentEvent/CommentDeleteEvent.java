package com.sprint.team2.monew.domain.userActivity.events.commentEvent;

import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CommentDeleteEvent {
  private final UUID commentId;
  private final UUID userId;

}
