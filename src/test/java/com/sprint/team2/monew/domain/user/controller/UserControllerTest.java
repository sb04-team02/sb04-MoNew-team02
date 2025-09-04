package com.sprint.team2.monew.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.team2.monew.domain.user.dto.request.UserLoginRequest;
import com.sprint.team2.monew.domain.user.dto.response.UserDto;
import com.sprint.team2.monew.domain.user.entity.User;
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
    @DisplayName("로그인 성공 테스트")
    void loginUser_Success() throws Exception {
        // 기본 객체 생성
        UUID userId = UUID.randomUUID();
        String email = "test@test.com";
        String nickname = "테스트";
        String password = "test1234";
        LocalDateTime createdAt = LocalDateTime.now();
        User user = new User(email, password, nickname);

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

}
