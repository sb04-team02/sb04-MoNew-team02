package com.sprint.team2.monew.domain.interest.mapper;

import com.sprint.team2.monew.domain.interest.dto.InterestDto;
import com.sprint.team2.monew.domain.interest.entity.Interest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InterestMapper {
    InterestDto toDto(Interest interest);
}
