package com.sprint.team2.monew.domain.user.service;

import com.sprint.team2.monew.domain.user.dto.request.UserLoginRequest;
import com.sprint.team2.monew.domain.user.dto.response.UserDto;

public interface UserService {

    UserDto login(UserLoginRequest request);
}
