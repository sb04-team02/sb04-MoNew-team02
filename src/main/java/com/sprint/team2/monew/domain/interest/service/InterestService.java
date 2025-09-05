package com.sprint.team2.monew.domain.interest.service;

import com.sprint.team2.monew.domain.interest.dto.InterestDto;
import com.sprint.team2.monew.domain.interest.dto.request.InterestRegisterRequest;

import java.util.UUID;

public interface InterestService {
    void unsubscribe(UUID interestId, UUID userId);
    InterestDto create(InterestRegisterRequest interestRegisterRequest);
}
