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
import com.sprint.team2.monew.domain.userActivity.events.userEvent.UserCreateEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BasicUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    ApplicationEventPublisher publisher;

    @InjectMocks
    private BasicUserService userService;

    // 생성 테스트
    @Test
    @DisplayName("사용자 생성 성공")
    void createUserSuccess() {
        // 기본 변수 및 객체 설정
        UUID userId = UUID.randomUUID();
        String email = "test@test.com";
        String password = "test1234";
        String nickname = "test";
        User user = new User(email, password, nickname);
        UserRegisterRequest request = new UserRegisterRequest(email, nickname, password);

        // given
        // 이메일 중복 검증 통과
        given(userRepository.existsByEmail(eq(email))).willReturn(false);

        // userRepository.save 반환 설정
        given(userRepository.save(any(User.class))).willReturn(user);

        // Dto
        UserDto userDto = new UserDto(
                userId,
                email,
                nickname,
                LocalDateTime.now()
        );
        given(userMapper.toEntity(request)).willReturn(user);
        given(userMapper.toDto(any(User.class))).willReturn(userDto);

        // 이벤트 퍼블리셔
        willDoNothing().given(publisher).publishEvent(any(UserCreateEvent.class));

        // when
        UserDto result = userService.create(request);

        // then
        assertThat(result).isEqualTo(userDto);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("이미 존재하는 이메일로 사용자 생성 시도 시 실패")
    void createUserWithExistingEmailThrowsException() {
        // 기본 변수 및 객체 설정
        String email = "test@test.com";
        String password = "test1234";
        String nickname = "test";
        UserRegisterRequest request = new UserRegisterRequest(email, nickname, password);

        // given
        // 이메일 중복 감지
        given(userRepository.existsByEmail(eq(email))).willReturn(true);

        // when & then
        // 이메일 중복 검증 실패 -> EmailAlreadyExistsException
        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    // 로그인 테스트
    @Test
    @DisplayName("사용자 로그인 성공")
    void loginUserSuccess() {
        // 기본 변수 및 객체 설정
        String email = "test@test.com";
        String password = "test1234";
        String nickname = "test";
        UserLoginRequest request = new UserLoginRequest(email, password);
        User user = new User(email, password, nickname);

        // Dto
        UserDto userDto = new UserDto(
                UUID.randomUUID(),
                email,
                nickname,
                LocalDateTime.now()
        );

        // given
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(userMapper.toDto(any(User.class))).willReturn(userDto);

        // when
        UserDto result = userService.login(request);

        // then
        assertThat(result).isEqualTo(userDto);
        verify(userRepository).findByEmail(eq(email));
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인 시도 시 실패")
    void loginUserInvalidEmailThrowsException() {
        // 기본 변수 및 객체 설정
        String email = "test@test.com";
        String password = "test1234";
        UserLoginRequest request = new UserLoginRequest(email, password);

        // given
        // 존재하지 않는 이메일 -> InvalidUserCredentialException
        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(LoginFailedException.class);
    }

    @Test
    @DisplayName("일치하지 않는 비밀번호로 로그인 시도 시 실패")
    void loginUserInvalidPasswordThrowsException() {
        // 기본 변수 및 객체 설정
        String email = "test@test.com";
        String password = "test1234";
        String nickname = "test";
        UserLoginRequest request = new UserLoginRequest(email, password);
        User user = new User(email, "test12345", nickname);

        // given
        // 이메일 검증 통과
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        // 비밀번호 검증 실패 (요청받은 비밀번호: test1234, 실제 비밀번호: test12345)

        // when & then
        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(LoginFailedException.class);
    }

    @Test
    @DisplayName("논리적 삭제된 사용자로 로그인 시도 시 실패")
    void loginUser_logicalDelete_ThrowsException() {
        // 기본 변수 및 객체 설정
        String email = "test@test.com";
        String password = "test1234";
        String nickname = "test";
        UserLoginRequest request = new UserLoginRequest(email, password);
        User user = new User(email, "test12345", nickname);
        user.setDeletedAt(LocalDateTime.now());

        // given
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        // when & then
        // user.getDeletedAt()의 결과가 null이 아님 -> LoginFailedException
        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(LoginFailedException.class);
    }

    // 업데이트 테스트
    @Test
    @DisplayName("사용자 정보 수정 성공")
    void updateUserSuccess() {
        // 기본 변수 및 객체 설정
        UserUpdateRequest request = new UserUpdateRequest("newNickname");
        UUID userId = UUID.randomUUID();
        UUID loginUserId = userId;

        String email = "test@test.com";
        String password = "test1234";
        String nickname = "test";
        User user = new User(email, password, nickname);

        // Dto
        UserDto userDto = new UserDto(
                userId,
                "test@test.com",
                "newNickname",
                LocalDateTime.now()
        );

        // given
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(userMapper.toDto(any(User.class))).willReturn(userDto);

        // when
        UserDto result = userService.update(userId, request, loginUserId);

        // then
        assertThat(result).isEqualTo(userDto);
    }

    @Test
    @DisplayName("로그인된 사용자와 다른 사용자 id로 정보 수정 시도 시 실패")
    void updateUserMismatchLoginUserIdThrowsException() {
        // 기본 변수 및 객체 설정
        UserUpdateRequest request = new UserUpdateRequest("newNickname");
        UUID userId = UUID.randomUUID();
        UUID loginUserId = UUID.randomUUID();

        // 로그인된 UUID와 정보 수정을 시도중인 UUID가 다름 -> ForbiddenUserAuthorityException

        // when & then
        assertThatThrownBy(() -> userService.update(userId, request, loginUserId))
                .isInstanceOf(ForbiddenUserAuthorityException.class);
    }

    @Test
    @DisplayName("유효하지 않은 사용자 id로 정보 수정 시도 시 실패")
    void updateUserInvalidUserIdThrowsException() {
        // 기본 변수 및 객체 설정
        UserUpdateRequest request = new UserUpdateRequest("newNickname");
        UUID userId = UUID.randomUUID();
        UUID loginUserId = userId;

        // given
        // 해당 UUID에 해당하는 유저를 찾지 못함 -> UserNotFoundException
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.update(userId, request, loginUserId))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("논리적 삭제된 사용자로 정보 수정 시도 시 실패")
    void updateUserLogicallyDeletedUserThrowsException() {
        // 기본 변수 및 객체 설정
        UserUpdateRequest request = new UserUpdateRequest("newNickname");
        UUID userId = UUID.randomUUID();
        UUID loginUserId = userId;

        String email = "test@test.com";
        String password = "test1234";
        String nickname = "test";
        User user = new User(email, password, nickname);
        user.setDeletedAt(LocalDateTime.now());

        // given
        // 해당 UUID에 해당하는 유저를 찾지 못함 -> UserNotFoundException
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> userService.update(userId, request, loginUserId))
                .isInstanceOf(UserNotFoundException.class);
    }

    // 삭제 테스트 - 논리 삭제
    @Test
    @DisplayName("논리적 사용자 삭제 성공")
    void deleteLogicallyUserSuccess() {
        // 기본 변수 및 객체 설정
        UUID userId = UUID.randomUUID();
        UUID loginUserId = userId;
        String email = "test@test.com";
        String password = "test1234";
        String nickname = "test";
        User user = new User(email, password, nickname);

        // given
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        userService.deleteLogically(userId, loginUserId);

        // then
        assertThat(user.getDeletedAt()).isNotNull();
        assertThat(user.getDeletedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("로그인된 사용자와 다른 사용자 id로 논리적 삭제 시도 시 실패")
    void deleteLogicallyUserMismatchLoginIdThrowsException() {
        // 기본 변수 및 객체 설정
        UUID userId = UUID.randomUUID();
        UUID loginUserId = UUID.randomUUID();

        // 로그인된 UUID와 논리적 삭제 시도중인 UUID가 다름 -> ForbiddenUserAuthorityException

        // when & then
        assertThatThrownBy(() -> userService.deleteLogically(userId, loginUserId))
                .isInstanceOf(ForbiddenUserAuthorityException.class);
    }

    @Test
    @DisplayName("존재하지 않는 사용자에 대해 논리적 삭제 시도 시 실패")
    void deleteLogicallyUserInvalidUserThrowsException() {
        // 기본 변수 및 객체 설정
        UUID userId = UUID.randomUUID();
        UUID loginUserId = userId;

        // given
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.deleteLogically(userId, loginUserId))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("이미 논리적 삭제된 사용자에 대해 논리적 삭제 다시 시도 시 실패")
    void deleteLogicallyUserLogicallyDeletedUserThrowsException() {
        // 기본 변수 및 객체 설정
        UUID userId = UUID.randomUUID();
        UUID loginUserId = userId;
        String email = "test@test.com";
        String password = "test1234";
        String nickname = "test";
        User user = new User(email, password, nickname);
        user.setDeletedAt(LocalDateTime.now());

        // given
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> userService.deleteLogically(userId, loginUserId))
                .isInstanceOf(UserNotFoundException.class);
    }

    // 삭제 테스트 - 물리 삭제
    @Test
    @DisplayName("물리적 삭제 성공")
    void deletePhysicallyUserSuccess() {
        // 기본 변수 및 객체 설정
        UUID userId = UUID.randomUUID();
        UUID loginUserId = userId;
        String email = "test@test.com";
        String password = "test1234";
        String nickname = "test";
        User user = new User(email, password, nickname);

        // given
        given(userRepository.existsById(userId)).willReturn(true);

        // when
        userService.deletePhysicallyByForce(userId, loginUserId);

        // then
        verify(userRepository).deleteById(userId);
    }

    @Test
    @DisplayName("로그인 사용자와 다른 사용자 id로 물리적 삭제 시도 시 실패")
    void deletePhysicallyUserMismatchLoginIdThrowsException() {
        // 기본 변수 및 객체 설정
        UUID userId = UUID.randomUUID();
        UUID loginUserId = UUID.randomUUID();

        // 로그인된 UUID와 논리적 삭제 시도중인 UUID가 다름 -> ForbiddenUserAuthorityException

        // when & then
        assertThatThrownBy(() -> userService.deletePhysicallyByForce(userId, loginUserId))
                .isInstanceOf(ForbiddenUserAuthorityException.class);
    }

    @Test
    @DisplayName("존재하지 않는 사용자에 대해 물리적 삭제 시도 시 실패")
    void deletePhysicallyUserInvalidUserThrowsException() {
        // 기본 변수 및 객체 설정
        UUID userId = UUID.randomUUID();
        UUID loginUserId = userId;

        // given
        given(userRepository.existsById(userId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> userService.deletePhysicallyByForce(userId, loginUserId))
                .isInstanceOf(UserNotFoundException.class);
    }
}
