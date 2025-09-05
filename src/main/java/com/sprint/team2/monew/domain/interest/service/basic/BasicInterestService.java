package com.sprint.team2.monew.domain.interest.service.basic;

import com.sprint.team2.monew.domain.interest.dto.InterestDto;
import com.sprint.team2.monew.domain.interest.dto.request.InterestRegisterRequest;
import com.sprint.team2.monew.domain.interest.entity.Interest;
import com.sprint.team2.monew.domain.interest.exception.InterestErrorCode;
import com.sprint.team2.monew.domain.interest.mapper.InterestMapper;
import com.sprint.team2.monew.domain.interest.repository.InterestRepository;
import com.sprint.team2.monew.domain.interest.service.InterestService;
import com.sprint.team2.monew.global.error.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class BasicInterestService implements InterestService {
    private final InterestRepository interestRepository;
    private final InterestMapper interestMapper;

    @Transactional
    @Override
    public InterestDto create(InterestRegisterRequest interestRegisterRequest) {
        log.info("[관심사] 생성 서비스 호출");
        validInterestName(interestRegisterRequest.name());
        Interest interest = interestMapper.toEntity(interestRegisterRequest);
        interestRepository.save(interest);
        InterestDto interestDto = interestMapper.toDto(interest);
        log.info("[관심사] 생성 완료, Id = {}", interestDto.id());
        return interestDto;
    }

    private void validInterestName(String name) {
        if (interestRepository.existsBySimilarityNameGreaterThan80Percent(name)){
            log.error("[관심사] 생성 실패: 유사한 관심사 존재 name = {}",name);
            throw new BusinessException(InterestErrorCode.INTEREST_ALREADY_EXISTS_SIMILARITY_NAME);
        }
    }




}
