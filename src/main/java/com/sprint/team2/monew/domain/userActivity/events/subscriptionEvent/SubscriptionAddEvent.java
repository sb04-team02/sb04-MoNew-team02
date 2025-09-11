package com.sprint.team2.monew.domain.userActivity.events.subscriptionEvent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SubscriptionAddEvent (
  UUID id, // subscription id
  UUID interestId,
  String interestName,
  List<String> interestKeywords,
  long interestSubscriberCount,
  LocalDateTime createdAt,
  UUID userId
){
}
