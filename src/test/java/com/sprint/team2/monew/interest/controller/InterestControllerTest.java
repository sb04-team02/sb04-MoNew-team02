package com.sprint.team2.monew.interest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.team2.monew.domain.interest.controller.InterestController;
import com.sprint.team2.monew.domain.interest.service.InterestService;
import com.sprint.team2.monew.domain.subscription.dto.SubscriptionDto;
import com.sprint.team2.monew.global.error.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InterestController.class)
@Import({GlobalExceptionHandler.class})
public class InterestControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper om;

    @MockitoBean
    private InterestService interestService;

    @DisplayName("유저는 관심사를 구독할 수 있습니다.")
    @Test
    void subscribeShouldSucceed() throws Exception {
        // 본문 생성
        UUID interestId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        // given
        SubscriptionDto subscriptionDto = new SubscriptionDto(UUID.randomUUID(), interestId, "name", List.of("keyword1", "keyword2"), 1, LocalDateTime.now());
        given(interestService.subscribe(interestId, userId)).willReturn(subscriptionDto);

        // when
        ResultActions resultActions = mockMvc.perform(
                multipart("/api/interests/{interest-id}/subscriptions",interestId)
                        .header("MoNew-Request-User-ID",userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
        );

        // then
        resultActions.andExpect(status().isOk());
    }

    @DisplayName("header에 MoNew-Request-User-ID가 존재하지 않을 시 값을 읽을 수 없어 실패한다")
    @Test
    void subscribeShouldFailWhenHeaderNotFound() throws Exception {
        UUID interestId = UUID.randomUUID();

        // when & then
        ResultActions resultActions = mockMvc.perform(
                multipart("/api/interests/{interest-id}/subscriptions",interestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
        );
        resultActions.andExpect(status().isInternalServerError());
    }
}
