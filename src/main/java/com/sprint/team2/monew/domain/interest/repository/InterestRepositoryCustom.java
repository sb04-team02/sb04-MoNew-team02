package com.sprint.team2.monew.domain.interest.repository;

import com.sprint.team2.monew.domain.interest.dto.request.CursorPageRequestInterestDto;
import com.sprint.team2.monew.domain.interest.dto.response.InterestQueryDto;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface InterestRepositoryCustom {
    Page<InterestQueryDto> findAllPage(CursorPageRequestInterestDto request, UUID userId);
}
