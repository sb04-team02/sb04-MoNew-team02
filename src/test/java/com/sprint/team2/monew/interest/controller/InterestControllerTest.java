package com.sprint.team2.monew.interest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.team2.monew.domain.interest.controller.InterestController;
import com.sprint.team2.monew.domain.interest.dto.InterestDto;
import com.sprint.team2.monew.domain.interest.dto.request.InterestRegisterRequest;
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

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
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
}
