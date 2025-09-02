package com.sprint.team2.monew.domain.user.service;

import java.util.UUID;

public interface UserService {
    UserDto create(UserRegisterRequest userRegisterRequest);

    UserDto login(UserLoginRequest userLoginRequest);

    UserDto update(UserUpdateRequest userUpdateRequest);

    void delete(UUID userId);
}
