package com.sprint.team2.monew.domain.interest.repository;

import com.sprint.team2.monew.domain.interest.dto.request.CursorPageRequestInterestDto;
import com.sprint.team2.monew.domain.interest.dto.response.InterestQueryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;

import java.util.UUID;

public interface InterestRepositoryCustom {
    Slice<InterestQueryDto> findAllPage(CursorPageRequestInterestDto request, UUID userId);
    Long countTotalElements(String keyword);

}
