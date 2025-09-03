package com.sprint.team2.monew.domain.user.mapper;

import com.sprint.team2.monew.domain.user.dto.response.UserDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);
}
