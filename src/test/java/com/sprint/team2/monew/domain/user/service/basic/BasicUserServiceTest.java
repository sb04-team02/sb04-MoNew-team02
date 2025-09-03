package com.sprint.team2.monew.domain.user.service.basic;

import com.sprint.team2.monew.domain.user.dto.request.UserRegisterRequest;
import com.sprint.team2.monew.domain.user.dto.response.UserDto;
import com.sprint.team2.monew.domain.user.entity.User;
import com.sprint.team2.monew.domain.user.exception.EmailAlreadyExistsException;
import com.sprint.team2.monew.domain.user.mapper.UserMapper;
import com.sprint.team2.monew.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
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

    private UUID userId;
    private String email;
    private String password;
    private String nickname;
    //
    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        email = "user@test.com";
        password = "password";
        nickname = "user";
        //
        user = new User(email, password, nickname);
        ReflectionTestUtils.setField(user, "id", userId);
        ReflectionTestUtils.setField(user, "createdAt", LocalDateTime.now());
        user.setUpdatedAt(user.getCreatedAt());
        //
        userDto = new UserDto(userId,
                email,
                nickname,
                user.getCreatedAt()
        );
    }

    @Test
    @DisplayName("사용자 생성 성공")
    void createUser_Success() {
        // given
        UserRegisterRequest request = new UserRegisterRequest(email, nickname, password);
        given(userRepository.existsByEmail(eq(email))).willReturn(false);
        given(userRepository.save(any(User.class))).willReturn(user);
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
        // given
        UserRegisterRequest request = new UserRegisterRequest(email, nickname, password);
        given(userRepository.existsByEmail(eq(email))).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }
}