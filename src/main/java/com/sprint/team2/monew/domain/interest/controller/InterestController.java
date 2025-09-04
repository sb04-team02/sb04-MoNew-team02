package com.sprint.team2.monew.domain.interest.controller;

import com.sprint.team2.monew.domain.interest.service.InterestService;
import com.sprint.team2.monew.domain.subscription.dto.SubscriptionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/interests")
public class InterestController {
    private final InterestService interestService;

    @PostMapping(value = "/{interest-id}/subscriptions")
    public ResponseEntity subscriptions(@PathVariable("interest-id") UUID interestId,
                                        @RequestHeader("MoNew-Request-User-ID") UUID userId) {
        log.info("[관심사] 구독 등록 호출 interestId = {}, userId = {}", interestId, userId);
        SubscriptionDto response = interestService.subscribe(interestId,userId);
        log.info("[관심사] 구독 등록 응답 완료 id = {}", response.id());
        return ResponseEntity.ok(response);
    }

}
