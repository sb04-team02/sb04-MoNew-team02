package com.sprint.team2.monew.domain.user.service.basic;

import com.sprint.team2.monew.domain.user.dto.request.UserRegisterRequest;
import com.sprint.team2.monew.domain.user.dto.response.UserDto;
import com.sprint.team2.monew.domain.user.exception.EmailAlreadyExistsException;
import com.sprint.team2.monew.domain.user.repository.UserRepository;
import com.sprint.team2.monew.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto create(UserRegisterRequest userRegisterRequest) {
        String email = userRegisterRequest.email();

        if (userRepository.existsByEmail(email)) {
            throw EmailAlreadyExistsException.emailDuplicated(email);
        }

        return new UserDto(UUID.randomUUID(),
                "email",
                "name",
                LocalDateTime.now());
    }
}
