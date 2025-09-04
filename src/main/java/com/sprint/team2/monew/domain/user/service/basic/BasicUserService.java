package com.sprint.team2.monew.domain.user.service.basic;

import com.sprint.team2.monew.domain.user.dto.request.UserRegisterRequest;
import com.sprint.team2.monew.domain.user.dto.response.UserDto;
import com.sprint.team2.monew.domain.user.entity.User;
import com.sprint.team2.monew.domain.user.exception.EmailAlreadyExistsException;
import com.sprint.team2.monew.domain.user.mapper.UserMapper;
import com.sprint.team2.monew.domain.user.repository.UserRepository;
import com.sprint.team2.monew.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto create(UserRegisterRequest userRegisterRequest) {
        log.debug("사용자 생성 시작: {}", userRegisterRequest);

        String email = userRegisterRequest.email();
        String password = userRegisterRequest.password();
        String nickname = userRegisterRequest.nickname();

        if (userRepository.existsByEmail(email)) {
            throw EmailAlreadyExistsException.emailDuplicated(email);
        }

        User savedUser = userRepository.save(new User(email, password, nickname));
        UserDto result = userMapper.toDto(savedUser);
        log.info("사용자 생성 완료: id={}", result.id());
        return result;
    }
}
