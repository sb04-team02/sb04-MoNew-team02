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
    public UserDto create(UserRegisterRequest request) {
        log.info("[사용자] 생성 시작 - email={}, nickname={}",
                request.email(),
                request.nickname()
        );

        String email = request.email();

        if (userRepository.existsByEmail(request.email())) {
            log.error("[사용자] 생성 실패 - 중복된 이메일 email={}", email);
            throw EmailAlreadyExistsException.emailDuplicated(email);
        }

        User savedUser = userRepository.save(userMapper.toEntity(request));
        UserDto result = userMapper.toDto(savedUser);
        log.info("[사용자] 생성 완료 - userId={}", result.id());
        return result;
    }
}
