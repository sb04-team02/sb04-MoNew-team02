package com.sprint.team2.monew.domain.interest.service;

import com.sprint.team2.monew.domain.interest.dto.InterestDto;
import com.sprint.team2.monew.domain.interest.dto.request.InterestRegisterRequest;

public interface InterestService {
    InterestDto create(InterestRegisterRequest interestRegisterRequest);
}
