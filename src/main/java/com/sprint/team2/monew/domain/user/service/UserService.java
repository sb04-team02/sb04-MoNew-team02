package com.sprint.team2.monew.domain.user.service;

import com.sprint.team2.monew.domain.user.dto.request.UserRegisterRequest;
import com.sprint.team2.monew.domain.user.dto.response.UserDto;

import com.sprint.team2.monew.domain.user.dto.request.UserLoginRequest;

public interface UserService {

    UserDto login(UserLoginRequest request);

    UserDto create(UserRegisterRequest request);
}
