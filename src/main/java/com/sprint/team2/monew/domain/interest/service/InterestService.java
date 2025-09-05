package com.sprint.team2.monew.domain.interest.service;

import com.sprint.team2.monew.domain.interest.dto.InterestDto;
import com.sprint.team2.monew.domain.interest.dto.request.InterestRegisterRequest;
import com.sprint.team2.monew.domain.subscription.dto.SubscriptionDto;

import java.util.UUID;

public interface InterestService {
    InterestDto create(InterestRegisterRequest interestRegisterRequest);
    SubscriptionDto subscribe(UUID interestId, UUID userId);
}
