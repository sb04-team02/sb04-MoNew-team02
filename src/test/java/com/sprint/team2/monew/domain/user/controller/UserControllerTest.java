package com.sprint.team2.monew.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.team2.monew.domain.user.dto.request.UserLoginRequest;
import com.sprint.team2.monew.domain.user.dto.request.UserRegisterRequest;
import com.sprint.team2.monew.domain.user.dto.response.UserDto;
import com.sprint.team2.monew.domain.user.exception.ForbiddenUserAuthorityException;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
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

    // 등록 테스트
    @Test
    @DisplayName("사용자 등록 성공 테스트")
    void createUser_Success() throws Exception {
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
        assertThat(userDto).isEqualTo(resultDto);
        resultActions.andExpect(status().isCreated());
    }

    @Test
    @DisplayName("사용자 등록 실패 테스트 - 유효하지 않은 요청")
    void createUser_Failure_InvalidRequest() throws Exception {
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

    // 로그인 테스트
    @Test
    @DisplayName("로그인 성공 테스트")
    void loginUser_Success() throws Exception {
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
        assertThat(userDto).isEqualTo(resultDto);
        resultActions.andExpect(status().isOk());
    }

    @Test
    @DisplayName("로그인 실패 테스트 - 유효하지 않은 요청")
    void loginUser_Failure_InvalidRequest() throws Exception {
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
    @DisplayName("논리적 삭제 성공 테스트")
    void deleteLogicallyUser_Success() throws Exception {
        // 기본 객체 생성
        
        // 요청 생성

        // 본문 객체 생성

        // given

        // when

        // then
    }

    @Test
    @DisplayName("논리적 삭제 실패 테스트 - 존재하지 않는 사용자")
    void deleteLogicallyUser_Failure_UserNotFound() throws Exception {
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

    @Test
    @DisplayName("논리적 삭제 실패 테스트 - 삭제 권한 없음")
    void deleteLogicallyUser_Failure_ForbiddenUserAuthority() throws Exception {
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
}
