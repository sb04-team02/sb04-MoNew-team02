package com.sprint.team2.monew.domain.interest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.team2.monew.domain.interest.controller.InterestController;
import com.sprint.team2.monew.domain.interest.dto.InterestDto;
import com.sprint.team2.monew.domain.interest.dto.request.InterestRegisterRequest;
import com.sprint.team2.monew.domain.interest.service.InterestService;
import com.sprint.team2.monew.domain.subscription.dto.SubscriptionDto;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

    @DisplayName("관심사를 등록한다")
    @Test
    void createInterest() throws Exception {
        // 본문 생성
        InterestRegisterRequest request = new InterestRegisterRequest("스포츠", List.of("축구","야구","농구"));
        String content = om.writeValueAsString(request);

        // given
        InterestDto interestDto = new InterestDto(UUID.randomUUID(), "스포츠", List.of("축구","야구","농구"), 0, false);
        given(interestService.create(request)).willReturn(interestDto);

        // when
        ResultActions resultActions = mockMvc.perform(
                multipart("/api/interests")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
        );

        // then
        resultActions.andExpect(jsonPath("$.name").value("스포츠"))
                .andExpect(jsonPath("$.keywords.size()").value(3))
                .andExpect(jsonPath("$.subscriberCount").value(0))
                .andExpect(jsonPath("$.subscribedByMe").value(false))
                .andExpect(status().isCreated());
    }

    @DisplayName("관심사 등록을 위해서는 최소 1개 이상의 키워드가 포함되어야 한다.")
    @Test
    void createInterestShouldFailWhenKeywordsIsEmpty() throws Exception {
        // 본문 생성
        InterestRegisterRequest request = new InterestRegisterRequest("스포츠", Collections.EMPTY_LIST);
        String content = om.writeValueAsString(request);

        // when
        ResultActions resultActions = mockMvc.perform(
                multipart("/api/interests")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
        );

        // then
        resultActions.andExpect(status().isBadRequest());
    }

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

    @DisplayName("관심사 ID를 받아서 삭제한다.")
    @Test
    void deleteInterestShouldSucceed() throws Exception {
        // 본문 생성
        UUID interestId = UUID.randomUUID();

        // given
        doNothing().when(interestService).delete(interestId);

        ResultActions resultActions = mockMvc.perform(
                delete("/api/interests/{interest-id}",interestId)
                        .accept(MediaType.APPLICATION_JSON)
        );
        resultActions.andExpect(status().isNoContent());
    }

    @DisplayName("관심사ID 형식이 올바르지 않으면 오류를 발생시킨다.")
    @Test
    void deleteInterestFailWhenInvalidInterestId() throws Exception {
        long interestId = 1123L;
        ResultActions resultActions = mockMvc.perform(
                delete("/api/interests/{interest-id}",interestId)
                        .accept(MediaType.APPLICATION_JSON)
        );
        resultActions.andExpect(status().isInternalServerError());
    }
}
