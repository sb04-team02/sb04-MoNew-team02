package com.sprint.team2.monew.domain.user.service.basic;

import com.sprint.team2.monew.domain.user.dto.request.UserLoginRequest;
import com.sprint.team2.monew.domain.user.dto.response.UserDto;
import com.sprint.team2.monew.domain.user.entity.User;
import com.sprint.team2.monew.domain.user.exception.InvalidUserCredentialsException;
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
}
