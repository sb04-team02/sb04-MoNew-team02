package com.sprint.team2.monew.domain.userActivity.events;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SubscriptionUpdatedEvent {
  UUID id;
  UUID interestId;
  String interestName;
  List<String> interestKeywords;
  long interestSubscriberCount;
  LocalDateTime createdAt;
}
