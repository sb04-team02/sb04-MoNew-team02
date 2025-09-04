package com.sprint.team2.monew.domain.user.service;

import com.sprint.team2.monew.domain.user.dto.request.UserRegisterRequest;
import com.sprint.team2.monew.domain.user.dto.response.UserDto;

public interface UserService {

    UserDto create(UserRegisterRequest request);
}
