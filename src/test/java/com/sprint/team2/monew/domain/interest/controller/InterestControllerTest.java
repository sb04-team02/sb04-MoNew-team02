package com.sprint.team2.monew.domain.interest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.team2.monew.domain.interest.controller.InterestController;
import com.sprint.team2.monew.domain.interest.dto.InterestDto;
import com.sprint.team2.monew.domain.interest.dto.request.CursorPageRequestInterestDto;
import com.sprint.team2.monew.domain.interest.dto.request.InterestRegisterRequest;
import com.sprint.team2.monew.domain.interest.dto.request.InterestUpdateRequest;
import com.sprint.team2.monew.domain.interest.dto.response.CursorPageResponseInterestDto;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
                delete("/api/interests/{interestId}",interestId)
                        .accept(MediaType.APPLICATION_JSON)
        );
        resultActions.andExpect(status().isNoContent());
    }

    @DisplayName("관심사ID 형식이 올바르지 않으면 오류를 발생시킨다.")
    @Test
    void deleteInterestFailWhenInvalidInterestId() throws Exception {
        long interestId = 1123L;
        ResultActions resultActions = mockMvc.perform(
                delete("/api/interests/{interestId}",interestId)
                        .accept(MediaType.APPLICATION_JSON)
        );
        resultActions.andExpect(status().isInternalServerError());
    }

    @DisplayName("관심사 ID와 검증된 키워드가 주어지면 해당 관심사의 키워드 수정이 성공적으로 이루어진다.")
    @Test
    void updateInterestKeywordShouldSucceedWhenValidateInterestIdAndKeywords() throws Exception {
        // 본문생성
        UUID interestId = UUID.randomUUID();
        InterestUpdateRequest interestUpdateRequest = new InterestUpdateRequest(List.of("updateKeyword1","updateKeyword2","updateKeyword3"));
        String content = om.writeValueAsString(interestUpdateRequest);
        InterestDto interestDto = new InterestDto(interestId,"name",List.of("updateKeyword1","updateKeyword2","updateKeyword3"),0L,false);
        given(interestService.update(interestId,interestUpdateRequest)).willReturn(interestDto);

        // when
        ResultActions resultActions = mockMvc.perform(
                patch("/api/interests/{interestId}",interestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content)
                        .accept(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("name"))
                .andExpect(jsonPath("$.keywords.size()").value(3))
                .andExpect(jsonPath("$.id").value(interestId.toString()));
    }

    @DisplayName("수정된 키워드가 하나도 존재하지 않으면 검증의 통과하지 못한다." +
            "키워드가 존재하지 않으면 수정할 수 없다.")
    @Test
    void updateInterestKeywordsShouldFailWithEmptyKeywords() throws Exception {
        // given
        UUID interestId = UUID.randomUUID();
        InterestUpdateRequest interestUpdateRequest = new InterestUpdateRequest(List.of());
        String content = om.writeValueAsString(interestUpdateRequest);

        // when & then
        ResultActions resultActions = mockMvc.perform(
                patch("/api/interests/{interestId}",interestId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
                .accept(MediaType.APPLICATION_JSON)
        );
        resultActions.andExpect(status().isBadRequest());
    }

    @DisplayName("커서 기반 페이지네이션을 구현하여 관심사 목록을 조회한다.")
    @Test
    void readAllInterestShouldSucceedWithCursorPagination() throws Exception{
        // 본문 생성
        String keyword = "스포츠";
        String orderBy = "name";
        String direction = "ASC";
        int limit = 50;
        UUID userId = UUID.randomUUID();
        CursorPageRequestInterestDto requestDto = new CursorPageRequestInterestDto(keyword, orderBy, direction, null, null, limit);
        // given
        CursorPageResponseInterestDto<InterestDto> response = new CursorPageResponseInterestDto<InterestDto>(List.of(new InterestDto(UUID.randomUUID(),"name",List.of("1","2"),1L,false)),"축구",LocalDateTime.now(),50,150L,true);
        given(interestService.readAll(requestDto,userId)).willReturn(response);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/interests")
                        .header("Monew-Request-User-Id",userId)
                        .param("keyword",keyword)
                        .param("orderBy",orderBy)
                        .param("direction",direction)
                        .param("limit", String.valueOf(50))
                        .accept(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isNotEmpty());

    }

    @DisplayName("orderBy, direction, limit은 필수로 포함되어야 하며, 포함하지 않을 경우 요청에 실패한다.")
    @Test
    void readAllInterestShouldFailWhenInvalidParam() throws Exception{
        // given
        UUID userId = UUID.randomUUID();

        // when & then
        ResultActions resultActions = mockMvc.perform(
                get("/api/interests")
                        .header("Monew-Request-User-Id",userId)
                        .accept(MediaType.APPLICATION_JSON)
        );
        resultActions.andExpect(status().isBadRequest());
    }

    @DisplayName("정렬 기준은 name,subscriberCount만 올 수 있으며 그 외에는 올 수 없다." +
            "그 외에 값이 들어올 경우 요청에 실패한다.")
    @Test
    void readAllInterestShouldFailWhenUndefinedValueInOrderBy() throws Exception{
        // given
        UUID userId = UUID.randomUUID();
        String keyword = "keyword";
        String orderBy = "createdAt";
        String direction = "ASC";
        int limit = 50;

        // when & then
        ResultActions resultActions = mockMvc.perform(
                get("/api/interests")
                        .header("Monew-Request-User-Id",userId)
                        .param("keyword",keyword)
                        .param("orderBy",orderBy)
                        .param("direction",direction)
                        .param("limit",String.valueOf(limit))
                        .accept(MediaType.APPLICATION_JSON)
        );
        resultActions.andExpect(status().isInternalServerError());
    }
}
