package com.sprint.team2.monew.domain.user.service.basic;

import com.sprint.team2.monew.domain.user.dto.request.UserLoginRequest;
import com.sprint.team2.monew.domain.user.dto.request.UserRegisterRequest;
import com.sprint.team2.monew.domain.user.dto.request.UserUpdateRequest;
import com.sprint.team2.monew.domain.user.dto.response.UserDto;
import com.sprint.team2.monew.domain.user.entity.User;
import com.sprint.team2.monew.domain.user.exception.EmailAlreadyExistsException;
import com.sprint.team2.monew.domain.user.exception.ForbiddenUserAuthorityException;
import com.sprint.team2.monew.domain.user.exception.LoginFailedException;
import com.sprint.team2.monew.domain.user.exception.UserNotFoundException;
import com.sprint.team2.monew.domain.user.mapper.UserMapper;
import com.sprint.team2.monew.domain.user.repository.UserRepository;
import com.sprint.team2.monew.domain.user.service.UserService;
import com.sprint.team2.monew.domain.userActivity.events.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    // User Activity 이벤트
    private final ApplicationEventPublisher publisher;

    @Override
    @Transactional
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

        // ============== User Activity 이벤트 추가 ==============
        publisher.publishEvent(new UserCreatedEvent(
            savedUser.getId(),
            savedUser.getEmail(),
            savedUser.getNickname(),
            savedUser.getCreatedAt()
        ));

        return result;
    }

    @Override
    @Transactional
    public UserDto login(UserLoginRequest request) {
        log.info("[사용자] 로그인 시작");
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.error("[사용자] 로그인 실패 - 이메일 혹은 비밀번호가 잘못됨");
                    return LoginFailedException.wrongEmailOrPassword();
                });

        if (user.getDeletedAt() != null) {
            // 논리적 삭제이지만 물리적 삭제인것처럼 동작 (존재하지 않는 이메일)
            log.error("[사용자] 로그인 실패 - 이메일 혹은 비밀번호가 잘못됨");
            throw LoginFailedException.wrongEmailOrPassword();
        }

        if (!user.getPassword().equals(request.password())) {
            log.error("[사용자] 로그인 실패 - 이메일 혹은 비밀번호가 잘못됨");
            throw LoginFailedException.wrongEmailOrPassword();
        }

        UserDto userDto = userMapper.toDto(user);
        log.info("[사용자] 로그인 성공 - id={}", userDto.id());
        return userDto;
    }

    @Override
    @Transactional
    public UserDto update(UUID userId, UserUpdateRequest request, UUID loginUserId) {
        log.info("[사용자] 정보 수정 시작");
        if (!loginUserId.equals(userId)) {
            log.error("[사용자] 정보 수정 실패 - 해당 사용자에 대한 권한이 없음 id={}, loginUserId={}",
                    userId,
                    loginUserId);
            throw ForbiddenUserAuthorityException.forUpdate(userId, loginUserId);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("[사용자] 정보 수정 실패 - 존재하지 않는 사용자 id={}", userId);
                    return UserNotFoundException.withId(userId);
                });

        // 논리적 삭제이지만 물리적 삭제인것처럼 동작
        if (user.getDeletedAt() != null) {
            log.error("[사용자] 정보 수정 실패 - 존재하지 않는 사용자 id={}", userId);
            throw UserNotFoundException.withId(userId);
        }

        log.debug("[사용자] 정보 수정 전 - id={}, nickname={}", userId, user.getNickname());
        user.update(request.nickname());
        UserDto userDto = userMapper.toDto(user);
        log.info("[사용자] 정보 수정 성공 - id={}, nickname={}", userDto.id(), userDto.nickname());
        return userDto;
    }

    @Override
    @Transactional
    public void deleteLogically(UUID userId, UUID loginUserId) {
        log.info("[사용자] 논리적 삭제 시작 - id={}", userId);
        if (!userId.equals(loginUserId)) {
            log.error("[사용자] 논리적 삭제 실패 - 해당 사용자에 대한 권한이 없음 id={}, loginUserId={}",
                    userId,
                    loginUserId);
            throw ForbiddenUserAuthorityException.forDelete(userId, loginUserId);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("[사용자] 논리적 삭제 실패 - 존재하지 않는 사용자 id={}", userId);
                    return UserNotFoundException.withId(userId);
                });

        if (user.getDeletedAt() != null) {
            log.error("[사용자] 논리적 삭제 실패 - 이미 삭제된 사용자 id={}", userId);
            throw UserNotFoundException.withId(userId);
        }

        user.setDeletedAt(LocalDateTime.now());
        log.debug("[사용자] 논리적 삭제 수행 - id={}, deletedAt={}", userId, user.getDeletedAt());
        log.info("[사용자] 논리적 삭제 성공 - id={}", userId);
    }

    @Override
    @Transactional
    public void deletePhysicallyByForce(UUID userId, UUID loginUserId) {
        log.info("[사용자] 물리적 삭제 시작 - id={}", userId);
        if (!userId.equals(loginUserId)) {
            log.error("[사용자] 물리적 삭제 실패 - 해당 사용자에 대한 권한이 없음 id={}, loginUserId={}",
                    userId,
                    loginUserId);
            throw ForbiddenUserAuthorityException.forDelete(userId, loginUserId);
        }

        if (!userRepository.existsById(userId)) {
            log.error("[사용자] 물리적 삭제 실패 - 존재하지 않는 사용자 id={}", userId);
            throw UserNotFoundException.withId(userId);
        }

        userRepository.deleteById(userId);
        log.info("[사용자] 물리적 삭제 성공 - id={}", userId);
    }

    @Override
    @Transactional
    public long deletePhysicallyByBatch() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(1);
        log.info("[사용자] 물리적 삭제 배치 처리 시작 - now={}, threshold={}", LocalDateTime.now(), threshold);
        
        List<User> users = userRepository.findAllByDeletedAtBefore(threshold);
        long count = users.size();
        log.debug("[사용자] 물리적 삭제 배치 처리 대상 유저 수 - userCount={}", count);

        userRepository.deleteAll(users);
        log.info("[사용자] 물리적 삭제 배치 처리 성공");
        return count; // 삭제된 건수 리턴
    }
}

