package com.sprint.team2.monew.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.team2.monew.domain.user.dto.request.UserLoginRequest;
import com.sprint.team2.monew.domain.user.dto.request.UserRegisterRequest;
import com.sprint.team2.monew.domain.user.dto.request.UserUpdateRequest;
import com.sprint.team2.monew.domain.user.dto.response.UserDto;
import com.sprint.team2.monew.domain.user.exception.EmailAlreadyExistsException;
import com.sprint.team2.monew.domain.user.exception.ForbiddenUserAuthorityException;
import com.sprint.team2.monew.domain.user.exception.LoginFailedException;
import com.sprint.team2.monew.domain.user.exception.UserNotFoundException;
import com.sprint.team2.monew.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    // 등록 API 테스트
    @Test
    @DisplayName("사용자 등록 성공 테스트")
    void createUserSuccess() throws Exception {
        // 기본 객체 생성
        UUID userId = UUID.randomUUID();
        String email = "test@test.com";
        String nickname = "테스트";
        String password = "test1234";
        LocalDateTime createdAt = LocalDateTime.now();

        // 요청 생성
        UserRegisterRequest request = new UserRegisterRequest(
                email,
                nickname,
                password
        );

        // 본문 객체 생성
        String content = objectMapper.writeValueAsString(request);

        // Dto 생성
        UserDto userDto = new UserDto(
                userId,
                email,
                nickname,
                createdAt
        );

        // given
        given(userService.create(request)).willReturn(userDto);

        // when
        ResultActions resultActions = mockMvc.perform(
                multipart("/api/users")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
        );

        // then
        MvcResult result = resultActions.andReturn();
        String json = result.getResponse().getContentAsString();
        UserDto resultDto = objectMapper.readValue(json, UserDto.class);
        assertThat(resultDto).isEqualTo(userDto);
        resultActions.andExpect(status().isCreated());
    }

    @Test
    @DisplayName("사용자 등록 실패 테스트 - 입력값 검증 실패")
    void createUserFailureInvalidRequest() throws Exception {
        // 요청 생성
        UserRegisterRequest request = new UserRegisterRequest(
                "invalid-email",  // 이메일 형식 위반
                "testtesttesttesttesttest", // 최대 길이 위반 (20자 이하)
                "test" // 비밀번호 정책 위반 (6자 이상)
        );
        String content = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(multipart("/api/users")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("사용자 등록 실패 테스트 - 이메일 중복")
    void createUserFailureDuplicateEmail() throws Exception {
        // 요청 생성
        String duplicatedEmail = "duplicatedEmail@email.com";
        UserRegisterRequest request = new UserRegisterRequest(
                duplicatedEmail, // 이메일 중복
                "test",
                "test1234"
        );
        String content = objectMapper.writeValueAsString(request);

        // given
        willThrow(EmailAlreadyExistsException.emailDuplicated(duplicatedEmail))
                .given(userService).create(eq(request));

        // when & then
        mockMvc.perform(multipart("/api/users")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    // 로그인 API 테스트
    @Test
    @DisplayName("로그인 성공 테스트")
    void loginUserSuccess() throws Exception {
        // 기본 객체 생성
        UUID userId = UUID.randomUUID();
        String email = "test@test.com";
        String nickname = "테스트";
        String password = "test1234";
        LocalDateTime createdAt = LocalDateTime.now();

        // 요청 생성
        UserLoginRequest request = new UserLoginRequest(email, password);

        // 본문 객체 생성
        String content = objectMapper.writeValueAsString(request);

        // Dto 생성
        UserDto userDto = new UserDto(
                userId,
                email,
                nickname,
                createdAt
        );

        // given
        given(userService.login(request)).willReturn(userDto);

        // when
        ResultActions resultActions = mockMvc.perform(
                multipart("/api/users/login")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
        );

        // then
        MvcResult result = resultActions.andReturn();
        String json = result.getResponse().getContentAsString();
        UserDto resultDto = objectMapper.readValue(json, UserDto.class);
        assertThat(resultDto).isEqualTo(userDto);
        resultActions.andExpect(status().isOk());
    }

    @Test
    @DisplayName("로그인 실패 테스트 - 입력값 검증 실패")
    void loginUserFailureInvalidRequest() throws Exception {
        // 요청 생성
        UserLoginRequest request = new UserLoginRequest(
                "invalid-email", // 이메일 형식 위반
                "testtesttesttesttesttest" // 최대 길이 위반 (20자 이하)
        );
        String content = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(multipart("/api/users/login")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("로그인 실패 테스트 - 이메일 또는 비밀번호 불일치")
    void loginUserFailureWrongEmailOrPassword() throws Exception {
        // 요청 생성
        UserLoginRequest request = new UserLoginRequest(
                "wrongEmail@email.com", // 잘못된 이메일
                "wrongPassword" // 잘못된 비밀번호
        );
        String content = objectMapper.writeValueAsString(request);

        // given
        willThrow(LoginFailedException.wrongEmailOrPassword())
                .given(userService).login(eq(request));

        // when & then
        mockMvc.perform(multipart("/api/users/login")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // 업데이트 API 테스트
    @Test
    @DisplayName("업데이트 성공 테스트")
    void updateUserSuccess() throws Exception {
        // 기본 객체 생성
        UUID userId = UUID.randomUUID();
        UUID loginUserId = userId;
        String email = "test@test.com";
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);

        // 요청 생성
        UserUpdateRequest request = new UserUpdateRequest("newNickname");

        // 본문 객체 생성
        String content = objectMapper.writeValueAsString(request);

        // Dto
        UserDto userDto = new UserDto(
                userId,
                email,
                "newNickname",
                createdAt
        );

        // given
        given(userService.update(userId, request, loginUserId)).willReturn(userDto);

        // when
        ResultActions resultActions = mockMvc.perform(
                multipart("/api/users/{userId}", userId)
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(httpRequest -> {
                            httpRequest.setMethod("PATCH");
                            return httpRequest;
                        })
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .header("Monew-Request-User-ID", loginUserId.toString())
        );

        // then
        MvcResult result = resultActions.andReturn();
        String json = result.getResponse().getContentAsString();
        UserDto resultDto = objectMapper.readValue(json, UserDto.class);
        assertThat(resultDto).isEqualTo(userDto);
        resultActions.andExpect(status().isOk());
    }

    @Test
    @DisplayName("업데이트 실패 테스트 - 입력값 검증 실패")
    void updateUserFailureInvalidRequest() throws Exception {
        // 요청 생성
        UUID userId = UUID.randomUUID();
        UserUpdateRequest request = new UserUpdateRequest("testtesttesttesttesttest");
        String content = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(multipart("/api/users/{userId}", userId)
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(httpRequest -> {
                            httpRequest.setMethod("PATCH");
                            return httpRequest;
                        }))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("업데이트 실패 테스트 - 사용자 정보 수정 권한 없음")
    void updateUserFailureForbiddenUserAuthority() throws Exception {
        // 요청 생성
        UUID userId = UUID.randomUUID();
        UUID loginUserId = UUID.randomUUID();
        UserUpdateRequest request = new UserUpdateRequest("testUser");
        String content = objectMapper.writeValueAsString(request);

        // given
        willThrow(ForbiddenUserAuthorityException.forUpdate(userId, loginUserId))
                .given(userService).update(userId, request, loginUserId);

        // when & then
        mockMvc.perform(multipart("/api/users/{userId}", userId)
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Monew-Request-User-ID", loginUserId.toString())
                        .with(httpRequest -> {
                            httpRequest.setMethod("PATCH");
                            return httpRequest;
                        }))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("업데이트 실패 테스트 - 사용자 정보 없음")
    void updateUserFailureUserNotFound() throws Exception {
        // 요청 생성
        UUID userId = UUID.randomUUID();
        UUID loginUserId = userId;
        UserUpdateRequest request = new UserUpdateRequest("testUser");
        String content = objectMapper.writeValueAsString(request);

        // given
        willThrow(UserNotFoundException.withId(userId))
                .given(userService).update(userId, request, loginUserId);

        // when & then
        mockMvc.perform(multipart("/api/users/{userId}", userId)
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Monew-Request-User-ID", loginUserId.toString())
                        .with(httpRequest -> {
                            httpRequest.setMethod("PATCH");
                            return httpRequest;
                        }))
                .andExpect(status().isNotFound());
    }

    // 논리적 삭제 API 테스트
    @Test
    @DisplayName("논리적 삭제 성공 테스트")
    void deleteLogicallyUserSuccess() throws Exception {
        // 기본 객체 생성
        UUID userId = UUID.randomUUID();
        UUID loginUserId = userId;

        // given
        willDoNothing().given(userService).deleteLogically(userId, loginUserId);

        // when & then
        mockMvc.perform(delete("/api/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Monew-Request-User-ID", loginUserId.toString()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("논리적 삭제 실패 테스트 - 삭제 권한 없음")
    void deleteLogicallyUserFailureForbiddenUserAuthority() throws Exception {
        // 기본 객체 생성
        UUID userId = UUID.randomUUID();
        UUID loginUserId = UUID.randomUUID();

        // given
        willThrow(ForbiddenUserAuthorityException.forDelete(userId, loginUserId))
                .given(userService).deleteLogically(userId, loginUserId);

        // when & then
        mockMvc.perform(delete("/api/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Monew-Request-User-ID", loginUserId.toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("논리적 삭제 실패 테스트 - 사용자 정보 없음")
    void deleteLogicallyUserFailureUserNotFound() throws Exception {
        // 기본 객체 생성
        UUID userId = UUID.randomUUID();
        UUID loginUserId = userId;

        // given
        willThrow(UserNotFoundException.withId(userId))
                .given(userService).deleteLogically(userId, loginUserId);

        // when & then
        mockMvc.perform(delete("/api/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Monew-Request-User-ID", loginUserId.toString()))
                .andExpect(status().isNotFound());
    }

    // 물리적 삭제 API 테스트
    @Test
    @DisplayName("물리적 삭제 성공 테스트")
    void deletePhysicallyByForceUserSuccess() throws Exception {
        // 기본 객체 생성
        UUID userId = UUID.randomUUID();
        UUID loginUserId = userId;

        // given
        willDoNothing().given(userService).deletePhysicallyByForce(userId, loginUserId);

        // when & then
        mockMvc.perform(delete("/api/users/{userId}/hard", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Monew-Request-User-ID", loginUserId.toString()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("물리적 삭제 실패 테스트 - 삭제 권한 없음")
    void deletePhysicallyByForceUserFailureForbiddenUserAuthority() throws Exception {
        // 기본 객체 생성
        UUID userId = UUID.randomUUID();
        UUID loginUserId = UUID.randomUUID();

        // given
        willThrow(ForbiddenUserAuthorityException.forDelete(userId, loginUserId))
                .given(userService).deletePhysicallyByForce(userId, loginUserId);

        // when & then
        mockMvc.perform(delete("/api/users/{userId}/hard", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Monew-Request-User-ID", loginUserId.toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("물리적 삭제 실패 테스트 - 사용자 정보 없음")
    void deletePhysicallyByForceUserFailureUserNotFound() throws Exception {
        // 기본 객체 생성
        UUID userId = UUID.randomUUID();
        UUID loginUserId = userId;

        // given
        willThrow(UserNotFoundException.withId(userId))
                .given(userService).deletePhysicallyByForce(userId, loginUserId);

        // when & then
        mockMvc.perform(delete("/api/users/{userId}/hard", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Monew-Request-User-ID", loginUserId.toString()))
                .andExpect(status().isNotFound());
    }
}
