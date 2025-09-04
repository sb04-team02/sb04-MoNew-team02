package com.sprint.team2.monew.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.team2.monew.domain.user.dto.request.UserRegisterRequest;
import com.sprint.team2.monew.domain.user.dto.response.UserDto;
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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
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
        UserDto responseDto = objectMapper.readValue(json, UserDto.class);

        assertAll(
                () -> assertEquals(userId, responseDto.id()),
                () -> assertEquals(email, responseDto.email()),
                () -> assertEquals(nickname, responseDto.nickname()),
                () -> assertEquals(createdAt, responseDto.createdAt())
        );
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
}