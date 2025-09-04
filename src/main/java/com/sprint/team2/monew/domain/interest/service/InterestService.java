package com.sprint.team2.monew.domain.interest.service;

import com.sprint.team2.monew.domain.subscription.dto.SubscriptionDto;

import java.util.UUID;

public interface InterestService {
    SubscriptionDto subscribe(UUID interestId, UUID userId);
}
