package com.sprint.team2.monew.domain.interest.controller;

import com.sprint.team2.monew.domain.interest.service.InterestService;
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

    @DeleteMapping(value = "/{interest-id}/subscriptions")
    public ResponseEntity unsubscribe(@PathVariable("interest-id") UUID interestId,
                                 @RequestHeader("Monew-Request-User-ID") UUID userId) {
        log.info("[구독] DELETE /api/interests/{interest-id}/subscriptions 구독 취소 API 호출");
        interestService.unsubscribe(interestId, userId);
        log.info("[구독] 구독 취소 응답 완료");
        return ResponseEntity.ok().build();
    }
}
