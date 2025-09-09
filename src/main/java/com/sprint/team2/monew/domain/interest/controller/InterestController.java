package com.sprint.team2.monew.domain.interest.controller;

import com.sprint.team2.monew.domain.interest.dto.InterestDto;
import com.sprint.team2.monew.domain.interest.dto.request.CursorPageRequestInterestDto;
import com.sprint.team2.monew.domain.interest.dto.request.InterestRegisterRequest;
import com.sprint.team2.monew.domain.interest.dto.request.InterestUpdateRequest;
import com.sprint.team2.monew.domain.interest.dto.response.CursorPageResponseInterestDto;
import com.sprint.team2.monew.domain.interest.service.InterestService;
import com.sprint.team2.monew.domain.subscription.dto.SubscriptionDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/interests")
public class InterestController {
    private final InterestService interestService;

    @PostMapping
    public ResponseEntity create(@Valid @RequestBody InterestRegisterRequest interestRegisterRequest) {
        log.info("[관심사] 생성 컨트롤러 호출");
        InterestDto response = interestService.create(interestRegisterRequest);
        log.info("[관심사] 생성 컨트롤러 응답 생성 id={}",response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/{interestId}/subscriptions")
    public ResponseEntity subscriptions(@PathVariable("interestId") UUID interestId,
                                        @RequestHeader("MoNew-Request-User-ID") UUID userId) {
        log.info("[관심사] 구독 등록 호출 interestId = {}, userId = {}", interestId, userId);
        SubscriptionDto response = interestService.subscribe(interestId,userId);
        log.info("[관심사] 구독 등록 응답 완료 id = {}", response.id());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(value = "/{interestId}/subscriptions")
    public ResponseEntity unsubscribe(@PathVariable("interestId") UUID interestId,
                                 @RequestHeader("Monew-Request-User-ID") UUID userId) {
        log.info("[구독] DELETE /api/interests/{interest-id}/subscriptions 구독 취소 API 호출");
        interestService.unsubscribe(interestId, userId);
        log.info("[구독] 구독 취소 응답 완료");
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(value = "/{interestId}")
    public ResponseEntity delete(@PathVariable("interestId") UUID interestId) {
        log.info("[관심사] 관심사 삭제 요청 id = {}", interestId);
        interestService.delete(interestId);
        log.info("[관심사] 관심사 삭제 응답 완료");
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(value = "/{interestId}")
    public ResponseEntity update(@PathVariable("interestId") UUID interestId,
                                 @Valid @RequestBody InterestUpdateRequest interestUpdateRequest) {
        log.info("[관심사] 관심사 수정 요청 id = {}", interestId);
        InterestDto response = interestService.update(interestId,interestUpdateRequest);
        log.info("[관심사] 관심사 수정 응답  완료 id = {}", response.id());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity readAll(@Valid @ModelAttribute CursorPageRequestInterestDto pageRequestDto,
            @RequestHeader("Monew-Request-User-Id") @NotNull UUID userId) {
        log.info("[관심사] 관심사 목록 조회 요청 userId = {}", userId);
        CursorPageResponseInterestDto<InterestDto> response = interestService.readAll(
                pageRequestDto, userId
        );
        log.info("[관심사] 관심사 목록 조회 응답 개수 = {}", response.content().size());
        return ResponseEntity.ok(response);
    }
}
