package com.sprint.team2.monew.interest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.team2.monew.domain.interest.controller.InterestController;
import com.sprint.team2.monew.domain.interest.service.InterestService;
import com.sprint.team2.monew.global.error.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

    @DisplayName("구독 취소 시 userId와 interestId를 통해 해당하는 구독을 취소합니다.")
    @Test
    void unsubscribe() throws Exception{
        // 본문 생성
        UUID userId = UUID.randomUUID();
        UUID interestId = UUID.randomUUID();

        // given
        doNothing().when(interestService).unsubscribe(userId, interestId);

        // when
        ResultActions resultActions = mockMvc.perform(
                delete("/api/interests/{interest-id}/subscriptions",interestId)
                        .header("Monew-Request-User-ID",userId)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
        );

        // then
        resultActions.andExpect(status().isOk());
    }

    @DisplayName("헤더에 Monew-Request-User-ID가 존재하지 않으면 실패합니다.")
    @Test
    void unsubscribeShouldFailWhenInvalidUserIdOrInterestId() throws Exception{
        // 본문 생성
        UUID userId = UUID.randomUUID();
        UUID interestId = UUID.randomUUID();

        // given
        doNothing().when(interestService).unsubscribe(userId, interestId);

        // when
        ResultActions resultActions = mockMvc.perform(
                delete("/api/interests/{interest-id}/subscriptions",interestId)
                        .header("Monew-Request-Fail-ID",userId)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
        );

        // then
        resultActions.andExpect(status().is5xxServerError());
    }
}
