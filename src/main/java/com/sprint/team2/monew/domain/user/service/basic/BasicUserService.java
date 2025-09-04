package com.sprint.team2.monew.domain.user.service.basic;

import com.sprint.team2.monew.domain.user.dto.request.UserLoginRequest;
import com.sprint.team2.monew.domain.user.dto.response.UserDto;
import com.sprint.team2.monew.domain.user.entity.User;
import com.sprint.team2.monew.domain.user.exception.InvalidUserCredentialsException;
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
    public UserDto login(UserLoginRequest request) {
        log.info("[사용자] 로그인 시작");
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                        log.error("[사용자] 로그인 실패 - 이메일 혹은 비밀번호가 잘못됨");
                        return InvalidUserCredentialsException.invalidEmailOrPassword();
                });

        if (!user.getPassword().equals(request.password())) {
            log.error("[사용자] 로그인 실패 - 이메일 혹은 비밀번호가 잘못됨");
            throw InvalidUserCredentialsException.invalidEmailOrPassword();
        }

        UserDto userDto = userMapper.toDto(user);
        log.info("[사용자] 로그인 성공 - id={}", userDto.id());
        return userDto;
    }
}
