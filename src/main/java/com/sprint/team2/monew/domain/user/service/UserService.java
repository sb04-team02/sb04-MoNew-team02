package com.sprint.team2.monew.domain.user.service;

import com.sprint.team2.monew.domain.user.dto.request.UserLoginRequest;
import com.sprint.team2.monew.domain.user.dto.request.UserRegisterRequest;
import com.sprint.team2.monew.domain.user.dto.request.UserUpdateRequest;
import com.sprint.team2.monew.domain.user.dto.response.UserDto;

import java.util.UUID;

public interface UserService {

    UserDto create(UserRegisterRequest request);

    UserDto login(UserLoginRequest request);

    UserDto update(UUID userId, UserUpdateRequest request, UUID loginUserId);

    void deleteLogically(UUID userId, UUID loginUserId);
}
