package com.sprint.team2.monew.domain.interest.service.basic;

import com.sprint.team2.monew.domain.interest.mapper.InterestMapper;
import com.sprint.team2.monew.domain.interest.repository.InterestRepository;
import com.sprint.team2.monew.domain.interest.service.InterestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicInterestService implements InterestService {
    private final InterestRepository interestRepository;
    private final InterestMapper interestMapper;
}
