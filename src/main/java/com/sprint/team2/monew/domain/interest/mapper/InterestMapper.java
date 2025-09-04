package com.sprint.team2.monew.domain.interest.mapper;

import com.sprint.team2.monew.domain.interest.dto.InterestDto;
import com.sprint.team2.monew.domain.interest.entity.Interest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InterestMapper {
    @Mapping(target = "subscribedByMe", constant = "false")
    InterestDto toDto(Interest interest);
}
