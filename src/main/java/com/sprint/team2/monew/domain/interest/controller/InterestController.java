package com.sprint.team2.monew.domain.interest.controller;

import com.sprint.team2.monew.domain.interest.dto.InterestDto;
import com.sprint.team2.monew.domain.interest.dto.request.InterestRegisterRequest;
import com.sprint.team2.monew.domain.interest.service.InterestService;
import com.sprint.team2.monew.domain.subscription.dto.SubscriptionDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping(value = "/{interest-id}/subscriptions")
    public ResponseEntity subscriptions(@PathVariable("interest-id") UUID interestId,
                                        @RequestHeader("MoNew-Request-User-ID") UUID userId) {
        log.info("[관심사] 구독 등록 호출 interestId = {}, userId = {}", interestId, userId);
        SubscriptionDto response = interestService.subscribe(interestId,userId);
        log.info("[관심사] 구독 등록 응답 완료 id = {}", response.id());
        return ResponseEntity.ok(response);
    }

}
