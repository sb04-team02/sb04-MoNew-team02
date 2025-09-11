package com.sprint.team2.monew.domain.userActivity.events.commentEvent;

import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public record CommentDeleteEvent (
  UUID commentId,
  UUID userId
) {
}
