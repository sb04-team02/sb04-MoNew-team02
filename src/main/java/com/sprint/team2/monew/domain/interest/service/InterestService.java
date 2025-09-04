package com.sprint.team2.monew.domain.interest.service;

import java.util.UUID;

public interface InterestService {
    void unsubscribe(UUID interestId, UUID userId);
}
