package com.sprint.team2.monew.domain.user.service.basic;

import com.sprint.team2.monew.domain.user.dto.request.UserLoginRequest;
import com.sprint.team2.monew.domain.user.dto.request.UserRegisterRequest;
import com.sprint.team2.monew.domain.user.dto.request.UserUpdateRequest;
import com.sprint.team2.monew.domain.user.dto.response.UserDto;
import com.sprint.team2.monew.domain.user.entity.User;
import com.sprint.team2.monew.domain.user.exception.EmailAlreadyExistsException;
import com.sprint.team2.monew.domain.user.exception.InvalidUserCredentialsException;
import com.sprint.team2.monew.domain.user.exception.UserNotFoundException;
import com.sprint.team2.monew.domain.user.mapper.UserMapper;
import com.sprint.team2.monew.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BasicUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private BasicUserService userService;

    // 생성 테스트
    @Test
    @DisplayName("사용자 생성 성공")
    void createUser_Success() {
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

        // when
        UserDto result = userService.create(request);

        // then
        assertThat(result).isEqualTo(userDto);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("이미 존재하는 이메일로 사용자 생성 시도 시 실패")
    void createUser_WithExistingEmail_ThrowsException() {
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
    void loginUser_Success() {
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
    void loginUser_InvalidEmail_ThrowsException() {
        // 기본 변수 및 객체 설정
        String email = "test@test.com";
        String password = "test1234";
        UserLoginRequest request = new UserLoginRequest(email, password);

        // given
        // 존재하지 않는 이메일 -> InvalidUserCredentialException
        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(InvalidUserCredentialsException.class);
    }

    @Test
    @DisplayName("일치하지 않는 비밀번호로 로그인 시도 시 실패")
    void loginUser_InvalidPassword_ThrowsException() {
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
                .isInstanceOf(InvalidUserCredentialsException.class);
    }

    @Test
    @DisplayName("")
    void updateUser_Success() {
        // 기본 변수 및 객체 설정
        UserUpdateRequest request = new UserUpdateRequest("newNickname");
        UUID userId = UUID.randomUUID();
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
        UserDto result = userService.update(userId, request);

        // then
        assertThat(result).isEqualTo(userDto);
    }

    @Test
    @DisplayName("유효하지 않은 사용자 id로 정보 수정 시도 시 실패")
    void updateUser_InvalidUserId_ThrowsException() {
        // 기본 변수 및 객체 설정
        UserUpdateRequest request = new UserUpdateRequest("newNickname");
        UUID userId = UUID.randomUUID();

        // given
        // 해당 UUID에 해당하는 유저를 찾지 못함 -> UserNotFoundException
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.update(userId, request))
                .isInstanceOf(UserNotFoundException.class);
    }
}
