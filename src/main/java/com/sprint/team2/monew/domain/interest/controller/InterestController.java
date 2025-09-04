package com.sprint.team2.monew.domain.interest.controller;

import com.sprint.team2.monew.domain.interest.dto.InterestDto;
import com.sprint.team2.monew.domain.interest.dto.request.InterestRegisterRequest;
import com.sprint.team2.monew.domain.interest.service.InterestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/interests")
public class InterestController {
    private final InterestService interestService;

    @PostMapping
    public ResponseEntity create(@Valid @RequestBody InterestRegisterRequest interestRegisterRequest) {
        log.info("POST /api/interests/create 호출");
        InterestDto response = interestService.create(interestRegisterRequest);
        log.debug("interest/create 응답 생성 id={}",response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
